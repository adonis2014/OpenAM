/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 * Copyright 2015 ForgeRock AS.
 */

package org.forgerock.openam.sts.rest.operation.cancel;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.sts.TokenCancellationException;
import org.forgerock.openam.sts.TokenMarshalException;
import org.forgerock.openam.sts.user.invocation.RestSTSTokenCancellationInvocationState;

/**
 * Encapsulates the concerns of cancelling rest-sts-issued tokens
 */
public interface IssuedTokenCancelOperation {
    /**
     * Note that in the 13 release, token cancellation involves invoking the STS TokenService to remove the specified token
     * from the CTS, which prevents subsequent successful validation.
     * @param invocationState The invocationState, as generated by the caller, containing the to-be-cancelled token state
     * @return JsonValue a message indicating that token cancellation as successful
     * @throws org.forgerock.openam.sts.TokenMarshalException if the token state corresponding to the to-be-cancelled token was incorrect
     * @throws org.forgerock.openam.sts.TokenCancellationException if an exception occurred which prevented token cancellation from occurring
     */
    JsonValue cancelToken(RestSTSTokenCancellationInvocationState invocationState) throws TokenMarshalException, TokenCancellationException;
}
