package com.tao.knowlegegraphtoefl.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.tao.knowlegegraphtoefl.ToeflApp
import com.tao.knowlegegraphtoefl.domain.model.ReviewCursor
import com.tao.knowlegegraphtoefl.domain.model.ReviewSessionState
import com.tao.knowlegegraphtoefl.domain.model.ReviewSkill
import com.tao.knowlegegraphtoefl.domain.model.ReviewTaskGroup
import com.tao.knowlegegraphtoefl.domain.model.ReviewTaskSession
import com.tao.knowlegegraphtoefl.domain.model.SkillResult
import com.tao.knowlegegraphtoefl.domain.repository.AttemptWriteResult
import com.tao.knowlegegraphtoefl.domain.repository.RemoteReviewTaskRepository
import com.tao.knowlegegraphtoefl.domain.repository.RemoteSubmitResult
import com.tao.knowlegegraphtoefl.domain.repository.ReviewTaskRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Reads and writes review task data directly against Cloud Firestore (real cloud project by
 * default, or the local emulator when enabled), so debug data survives rebuilds. This replaces
 * the previous Cloud Functions callable approach, since deploying Functions requires the Blaze
 * billing plan.
 */
class FirebaseReviewTaskRepository @Inject constructor() :
    ReviewTaskRepository,
    RemoteReviewTaskRepository {

    private val firebaseApp = ToeflApp.activeFirebaseApp()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(firebaseApp)
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(firebaseApp)

    override suspend fun loadReviewTaskGroup(): ReviewTaskSession {
        ensureSignedIn()
        val existingSessionId = scholarRef()
            .collection("learningState").document("current")
            .get().await()
            .takeIf { it.exists() }
            ?.getString("activeSessionId")
        if (existingSessionId != null) {
            val existing = scholarRef()
                .collection("reviewSessions").document(existingSessionId)
                .get().await()
            if (existing.exists() && existing.getString("bookId") == BOOK_ID) {
                return existing.toSession()
            }
        }
        val groups = listGroups()
        if (groups.isEmpty()) error("No active review group was found.")
        return createSession(groups.first())
    }

    override suspend fun submitReviewTask(
        session: ReviewTaskSession,
        result: SkillResult
    ): RemoteSubmitResult {
        ensureSignedIn()
        val skill = session.skills.firstOrNull { it.code == result.skillCode }
            ?: error("Unknown skill code.")
        val sessionRef = scholarRef().collection("reviewSessions").document(session.sessionId)
        val attemptRef = scholarRef().collection("studyAttempts").document(result.attemptId)
        val skillStateRef = scholarRef().collection("skillStates")
            .document("${result.sentenceId}_${result.skillCode}")
        val learningStateRef = scholarRef().collection("learningState").document("current")

        val groups = listGroups()
        return firestore.runTransaction { transaction ->
            val sessionSnapshot = transaction.get(sessionRef)
            if (!sessionSnapshot.exists()) error("Session is not active.")
            val attemptSnapshot = transaction.get(attemptRef)
            if (attemptSnapshot.exists()) error("Attempt was already processed.")

            val sentenceIds = sessionSnapshot.get("sentenceIds") as? List<*> ?: emptyList<Any>()
            val skillCodes = sessionSnapshot.get("skillCodes") as? List<*> ?: emptyList<Any>()
            val sentenceIndex = (sessionSnapshot.getLong("sentenceIndex") ?: 0L).toInt()
            val skillIndex = (sessionSnapshot.getLong("skillIndex") ?: 0L).toInt()
            val state = sessionSnapshot.getString("state")
            if (state != "active") error("Session is not active.")
            val currentSentenceId = sentenceIds.getOrNull(sentenceIndex) as? String
            val currentSkillCode = skillCodes.getOrNull(skillIndex) as? String
            if (result.sentenceId != currentSentenceId || result.skillCode != currentSkillCode) {
                error("The submitted card is stale.")
            }

            val passed = result.score >= skill.passScore && result.confidence >= skill.minConfidence
            val groupId = sessionSnapshot.getString("groupId")
            val hasNextGroup = groups.indexOfFirst { it.groupId == groupId }
                .let { it in 0 until groups.size - 1 }

            val outcome: RemoteSubmitResult
            val sessionUpdate: Map<String, Any>
            if (passed) {
                val nextSkillIndex = skillIndex + 1
                if (nextSkillIndex < skillCodes.size) {
                    val cursor = ReviewCursor(sentenceIndex, nextSkillIndex)
                    outcome = RemoteSubmitResult.Advanced(cursor)
                    sessionUpdate = mapOf("sentenceIndex" to sentenceIndex, "skillIndex" to nextSkillIndex)
                } else {
                    val nextSentenceIndex = sentenceIndex + 1
                    if (nextSentenceIndex < sentenceIds.size) {
                        val cursor = ReviewCursor(nextSentenceIndex, 0)
                        outcome = RemoteSubmitResult.Advanced(cursor)
                        sessionUpdate = mapOf("sentenceIndex" to nextSentenceIndex, "skillIndex" to 0)
                    } else {
                        outcome = RemoteSubmitResult.Completed(hasNextGroup)
                        sessionUpdate = mapOf("state" to "group_completed")
                    }
                }
            } else {
                outcome = RemoteSubmitResult.Retried(
                    cursor = ReviewCursor(sentenceIndex, skillIndex),
                    score = result.score,
                    feedback = "Keep practicing this skill and try again."
                )
                sessionUpdate = emptyMap()
            }

            transaction.set(
                attemptRef,
                mapOf(
                    "attemptId" to result.attemptId,
                    "sentenceId" to result.sentenceId,
                    "skillCode" to result.skillCode,
                    "score" to result.score,
                    "confidence" to result.confidence,
                    "attemptStatus" to if (passed) "correct" else "incorrect",
                    "timeSpentSeconds" to result.timeSpentSeconds,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            transaction.set(
                skillStateRef,
                mapOf(
                    "sentenceId" to result.sentenceId,
                    "skillCode" to result.skillCode,
                    "status" to if (passed) "mastered" else "learning",
                    "masteryScore" to result.score,
                    "progress" to result.score / 100f,
                    "attemptCount" to FieldValue.increment(1),
                    "lastStudiedAt" to FieldValue.serverTimestamp(),
                    "nextReviewAt" to FieldValue.serverTimestamp(),
                    "confidence" to result.confidence,
                    "stability" to 0,
                    "difficulty" to 1
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
            if (sessionUpdate.isNotEmpty()) {
                transaction.update(sessionRef, sessionUpdate + ("updatedAt" to FieldValue.serverTimestamp()))
            } else {
                transaction.update(sessionRef, "updatedAt", FieldValue.serverTimestamp())
            }
            transaction.set(
                learningStateRef,
                mapOf(
                    "bookId" to BOOK_ID,
                    "currentGroupId" to groupId,
                    "activeSessionId" to session.sessionId,
                    "lastStudiedAt" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
            outcome
        }.await()
    }

    override suspend fun appendAttempt(result: SkillResult): AttemptWriteResult =
        error("Firebase submitReviewTask is the only write path")

    override suspend fun upsertSkillState(result: SkillResult, passed: Boolean) =
        error("Firebase submitReviewTask is the only write path")

    override suspend fun loadNextGroup(previousSessionId: String): ReviewTaskSession? {
        ensureSignedIn()
        val previous = scholarRef().collection("reviewSessions").document(previousSessionId)
            .get().await()
        if (!previous.exists()) error("Previous session does not belong to this user.")
        val previousGroupId = previous.getString("groupId")
        val groups = listGroups()
        val index = groups.indexOfFirst { it.groupId == previousGroupId }
        val nextGroup = if (index >= 0) groups.getOrNull(index + 1) else null
        return nextGroup?.let { createSession(it) }
    }

    private suspend fun createSession(group: FirestoreGroup): ReviewTaskSession {
        if (group.sentenceIds.isEmpty()) error("The selected group has no sentences.")
        val sessionRef = scholarRef().collection("reviewSessions").document()
        val session = mapOf(
            "bookId" to BOOK_ID,
            "groupId" to group.groupId,
            "title" to group.title,
            "sentenceIds" to group.sentenceIds,
            "skillCodes" to SKILLS.map { it.code },
            "sentenceIndex" to 0,
            "skillIndex" to 0,
            "state" to "active",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        sessionRef.set(session).await()
        scholarRef().collection("learningState").document("current").set(
            mapOf(
                "bookId" to BOOK_ID,
                "currentGroupId" to group.groupId,
                "activeSessionId" to sessionRef.id,
                "lastStudiedAt" to FieldValue.serverTimestamp(),
                "status" to "learning"
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
        return ReviewTaskSession(
            sessionId = sessionRef.id,
            group = ReviewTaskGroup(group.groupId, group.title, group.sentenceIds),
            cursor = ReviewCursor(0, 0),
            skills = SKILLS,
            state = ReviewSessionState.ACTIVE
        )
    }

    private suspend fun listGroups(): List<FirestoreGroup> {
        val chapters = firestore.collection("books").document(BOOK_ID)
            .collection("chapters").get().await()
        val groups = mutableListOf<FirestoreGroup>()
        for (chapter in chapters.documents) {
            val lessons = chapter.reference.collection("lessons").get().await()
            for (lesson in lessons.documents) {
                val lessonGroups = lesson.reference.collection("groups")
                    .whereEqualTo("active", true).get().await()
                for (group in lessonGroups.documents) {
                    val sentenceIds = (group.get("sentenceIds") as? List<*>)
                        ?.filterIsInstance<String>() ?: emptyList()
                    groups.add(
                        FirestoreGroup(
                            groupId = group.id,
                            title = group.getString("title") ?: "",
                            sentenceIds = sentenceIds,
                            orderInLesson = (group.getLong("orderInLesson") ?: 0L).toInt(),
                            lessonOrder = (lesson.getLong("orderInLesson") ?: 0L).toInt(),
                            chapterOrder = (chapter.getLong("orderInChapter") ?: 0L).toInt()
                        )
                    )
                }
            }
        }
        return groups.sortedWith(
            compareBy({ it.chapterOrder }, { it.lessonOrder }, { it.orderInLesson }, { it.groupId })
        )
    }

    private fun scholarRef() =
        firestore.collection("scholars").document(requireNotNull(auth.currentUser).uid)

    private suspend fun ensureSignedIn() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }

    private data class FirestoreGroup(
        val groupId: String,
        val title: String,
        val sentenceIds: List<String>,
        val orderInLesson: Int,
        val lessonOrder: Int,
        val chapterOrder: Int
    )

    private companion object {
        const val BOOK_ID = "toefl-tpo"
        val SKILLS = listOf(
            ReviewSkill("translation", "Translation", passScore = 60, minConfidence = 0.6f),
            ReviewSkill("listening", "Listening", passScore = 60, minConfidence = 0.6f),
            ReviewSkill("speaking", "Speaking", passScore = 60, minConfidence = 0.6f)
        )
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toSession(): ReviewTaskSession {
    val sentenceIds = (get("sentenceIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val sentenceIndex = (getLong("sentenceIndex") ?: 0L).toInt()
    val skillIndex = (getLong("skillIndex") ?: 0L).toInt()
    val state = when (getString("state")) {
        "group_completed" -> ReviewSessionState.GROUP_COMPLETED
        else -> ReviewSessionState.ACTIVE
    }
    return ReviewTaskSession(
        sessionId = id,
        group = ReviewTaskGroup(
            groupId = getString("groupId") ?: "",
            title = getString("title") ?: "",
            sentenceIds = sentenceIds
        ),
        cursor = ReviewCursor(sentenceIndex, skillIndex),
        skills = listOf(
            ReviewSkill("translation", "Translation", passScore = 60, minConfidence = 0.6f),
            ReviewSkill("listening", "Listening", passScore = 60, minConfidence = 0.6f),
            ReviewSkill("speaking", "Speaking", passScore = 60, minConfidence = 0.6f)
        ),
        state = state
    )
}
