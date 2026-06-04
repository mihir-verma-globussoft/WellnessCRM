package com.globussoft.wellness.patient.feature.membership.domain.repository

import com.globussoft.wellness.patient.feature.membership.domain.model.Membership
import com.globussoft.wellness.patient.feature.membership.domain.model.MembershipPlan

interface MembershipRepository {
    suspend fun getMyMemberships(): List<Membership>
    suspend fun getCachedMemberships(): List<Membership>
    suspend fun getMembershipPlans(): List<MembershipPlan>
}
