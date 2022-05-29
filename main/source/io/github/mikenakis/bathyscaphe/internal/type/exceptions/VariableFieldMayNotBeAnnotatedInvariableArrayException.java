/*
 * Copyright (c) 2022 Michael Belivanakis a.k.a. MikeNakis, michael.gr
 *
 * For licensing information, please see LICENSE.md.
 * You may not use this file except in compliance with the license.
 */

package io.github.mikenakis.bathyscaphe.internal.type.exceptions;

import io.github.mikenakis.bathyscaphe.annotations.InvariableArray;
import io.github.mikenakis.bathyscaphe.internal.mykit.UncheckedException;

import java.lang.reflect.Field;

/**
 * Thrown if a variable field is annotated with the @{@link InvariableArray} annotation.
 * The @{@link InvariableArray} annotation is only valid for invariable fields.
 *
 * @author michael.gr
 */
public class VariableFieldMayNotBeAnnotatedInvariableArrayException extends UncheckedException
{
	public final Field field;

	public VariableFieldMayNotBeAnnotatedInvariableArrayException( Field field )
	{
		this.field = field;
	}
}
