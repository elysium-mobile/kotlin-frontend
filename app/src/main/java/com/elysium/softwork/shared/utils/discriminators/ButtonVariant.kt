package com.elysium.softwork.shared.utils.discriminators

/**
 * Visual variant of `SoftWorkButton`.
 *
 * **Category — discriminator enum.** No payload, no string keys; the entries exist purely
 * to drive a branch in the composable's rendering logic.
 *
 * - [EMPLOYEE] — sky-to-teal horizontal gradient. Default. Used for all Employee-facing
 *   primary actions (sign in, submit check-in, post to forum, etc.).
 * - [HR] — solid `PrimaryNavy`. Reserved for HR-themed primary actions surfaced to the
 *   Employee (e.g. responding to an HR-initiated request).
 */
enum class ButtonVariant { EMPLOYEE, HR }
