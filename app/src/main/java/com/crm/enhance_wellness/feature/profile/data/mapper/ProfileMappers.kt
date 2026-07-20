package com.crm.enhance_wellness.feature.profile.data.mapper

import com.crm.enhance_wellness.feature.profile.data.remote.dto.AuthProfileResponseDto
import com.crm.enhance_wellness.feature.profile.data.remote.dto.ProfileDto
import com.crm.enhance_wellness.feature.profile.domain.model.Profile

fun ProfileDto.toDomain() = Profile(
    patientId = id,
    name = name,
    phone = phone,
    email = email,
    dob = dob,
    gender = gender,
)

fun AuthProfileResponseDto.mergeInto(profile: Profile) = profile.copy(
    name = name,
    email = email ?: profile.email,
    profilePictureUrl = profilePicture ?: profile.profilePictureUrl,
)
