package mikenakis.assessment.internal.type.exceptions;

import mikenakis.assessment.annotations.Invariable;
import mikenakis.assessment.internal.mykit.UncheckedException;

import java.lang.reflect.Field;

/**
 * Thrown when a non-private field is annotated with @{@link Invariable}.
 * A class is not in a position of making any guarantees about the invariability of a field if the field is not private.
 *
 * @author michael.gr
 */
public class AnnotatedInvariableFieldMustBePrivateException extends UncheckedException
{
	public final Field field;

	public AnnotatedInvariableFieldMustBePrivateException( Field field )
	{
		this.field = field;
	}
}
