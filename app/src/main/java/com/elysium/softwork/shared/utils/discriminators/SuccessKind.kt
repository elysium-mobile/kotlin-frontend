package com.elysium.softwork.shared.utils.discriminators

/**
 * Tags the two surfaces that route into the shared `AuthSuccessScreen`.
 *
 * **Category — discriminator enum.** Used purely as a sum type passed through the IAM
 * navigation graph; the entry name itself is serialized into the nav-arg string so adding
 * a new entry implies updating the navigation route mapping in `AuthRoutes.success(...)`.
 */
enum class SuccessKind { LOGIN, REGISTER }
