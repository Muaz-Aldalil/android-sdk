package com.subulalhuda.data.model

import kotlinx.serialization.Serializable

/**
 * Social media links and contact information.
 * Derived from the website's src/constants/SOCIAL_LINKS.jsx — data portion only (no JSX/SVG).
 *
 * Icon keys use English strings (youtube, facebook, etc.) instead of Arabic strings
 * from the website source, to avoid RTL key-normalization issues.
 * The mapping from Arabic keys:
 *   يوتيوب → youtube
 *   فيسبوك → facebook
 *   تيك توك → tiktok
 *   تيليجرام → telegram
 */
@Serializable
data class SocialLinks(
    val socialLinks: List<SocialLink>,
    val contactInfo: ContactInfo,
)

@Serializable
data class SocialLink(
    val label: String,
    val href: String,
    val iconKey: String, // English key: "youtube", "facebook", "tiktok", "telegram"
)

@Serializable
data class ContactInfo(
    val phone: String,
    val location: String,
    val youtube: String,
    val facebook: String,
    val facebook2: String,
    val telegram: String,
    val tiktok: String,
)
