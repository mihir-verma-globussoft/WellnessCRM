package com.crm.enhance_wellness.feature.membership.domain.repository

import com.crm.enhance_wellness.feature.membership.domain.model.Membership
import com.crm.enhance_wellness.feature.membership.domain.model.MembershipPlan

interface MembershipRepository {
    suspend fun getMyMemberships(): List<Membership>
    suspend fun getCachedMemberships(): List<Membership>
    suspend fun getMembershipPlans(): List<MembershipPlan>
}
