package com.example.androidinterview.authentication

/**
 * Scripted [AuthenticationProvider].
 *
 * @param result Returned by every [authenticate] call.
 */
class FakeAuthenticationProvider(
    var result: AuthenticationResult = AuthenticationResult.Succeeded,
) : AuthenticationProvider {

    /** How many times the user was asked to authenticate. */
    var calls = 0

    override suspend fun authenticate(): AuthenticationResult {
        calls++
        return result
    }
}
