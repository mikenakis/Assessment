package mikenakis.assessment.internal.type.assessments.nonimmutable.mutable;

import mikenakis.assessment.internal.assessments.Assessment;
import mikenakis.assessment.internal.type.field.assessments.mutable.MutableFieldAssessment;

import java.util.List;

/**
 * Signifies that a class is mutable because it has mutable fields.
 */
public class MutableFieldMutableTypeAssessment extends MutableTypeAssessment
{
	public final MutableFieldAssessment fieldAssessment;

	public MutableFieldMutableTypeAssessment( Class<?> jvmClass, MutableFieldAssessment fieldAssessment )
	{
		super( jvmClass );
		assert fieldAssessment.field.getDeclaringClass() == jvmClass;
		this.fieldAssessment = fieldAssessment;
	}

	@Override public List<Assessment> children() { return List.of( fieldAssessment ); }
}
