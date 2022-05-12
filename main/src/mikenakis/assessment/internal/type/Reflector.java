package mikenakis.assessment.internal.type;

import mikenakis.assessment.internal.helpers.Helpers;
import mikenakis.assessment.internal.type.assessments.ImmutableTypeAssessment;
import mikenakis.assessment.internal.type.assessments.TypeAssessment;
import mikenakis.assessment.internal.type.assessments.UnderAssessmentTypeAssessment;
import mikenakis.assessment.internal.type.assessments.nonimmutable.mutable.ArrayMutableTypeAssessment;
import mikenakis.assessment.internal.type.assessments.nonimmutable.mutable.MultiReasonMutableTypeAssessment;
import mikenakis.assessment.internal.type.assessments.nonimmutable.mutable.MutableFieldMutableTypeAssessment;
import mikenakis.assessment.internal.type.assessments.nonimmutable.mutable.MutableSuperclassMutableTypeAssessment;
import mikenakis.assessment.internal.type.assessments.nonimmutable.mutable.MutableTypeAssessment;
import mikenakis.assessment.internal.type.assessments.nonimmutable.provisory.ExtensibleProvisoryTypeAssessment;
import mikenakis.assessment.internal.type.assessments.nonimmutable.provisory.InterfaceProvisoryTypeAssessment;
import mikenakis.assessment.internal.type.assessments.nonimmutable.provisory.MultiReasonProvisoryTypeAssessment;
import mikenakis.assessment.internal.type.assessments.nonimmutable.provisory.ProvisoryFieldProvisoryTypeAssessment;
import mikenakis.assessment.internal.type.assessments.nonimmutable.provisory.ProvisorySuperclassProvisoryTypeAssessment;
import mikenakis.assessment.internal.type.assessments.nonimmutable.provisory.ProvisoryTypeAssessment;
import mikenakis.assessment.internal.type.assessments.nonimmutable.provisory.SelfAssessableProvisoryTypeAssessment;
import mikenakis.assessment.internal.type.exceptions.SelfAssessableAnnotationIsOnlyApplicableToClassException;
import mikenakis.assessment.internal.type.exceptions.SelfAssessableClassMustNotBeImmutableException;
import mikenakis.assessment.internal.type.field.FieldAssessor;
import mikenakis.assessment.internal.type.field.assessments.FieldAssessment;
import mikenakis.assessment.internal.type.field.assessments.ImmutableFieldAssessment;
import mikenakis.assessment.internal.type.field.assessments.UnderAssessmentFieldAssessment;
import mikenakis.assessment.internal.type.field.assessments.mutable.MutableFieldAssessment;
import mikenakis.assessment.internal.type.field.assessments.provisory.ProvisoryFieldTypeProvisoryFieldAssessment;
import mikenakis.assessment.ImmutabilitySelfAssessable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Deeply assesses the nature of types using reflection. DO NOT USE. FOR INTERNAL USE ONLY.
 *
 * @author michael.gr
 */
final class Reflector
{
	private final TypeAssessor typeAssessor;
	private final FieldAssessor fieldAssessor;

	Reflector( TypeAssessor typeAssessor )
	{
		this.typeAssessor = typeAssessor;
		fieldAssessor = new FieldAssessor( typeAssessor );
	}

	TypeAssessment assess( Class<?> type )
	{
		TypeAssessment assessment = assess0( type );
		if( ImmutabilitySelfAssessable.class.isAssignableFrom( type ) )
		{
			assert Helpers.isClass( type ) : new SelfAssessableAnnotationIsOnlyApplicableToClassException( type );
			assert !(assessment instanceof ImmutableTypeAssessment) : new SelfAssessableClassMustNotBeImmutableException( type );
			return new SelfAssessableProvisoryTypeAssessment( type );
		}
		return assessment;
	}

	private TypeAssessment assess0( Class<?> type )
	{
		if( type.isArray() )
			return new ArrayMutableTypeAssessment( type );
		if( type.isInterface() )
			return new InterfaceProvisoryTypeAssessment( type );

		List<ProvisoryTypeAssessment> provisoryReasons = new ArrayList<>();
		List<MutableTypeAssessment> mutableReasons = new ArrayList<>();
		Class<?> superclass = type.getSuperclass();
		if( superclass != null )
		{
			TypeAssessment superclassAssessment = assessSuperclass( superclass );
			switch( superclassAssessment )
			{
				case MutableTypeAssessment mutableTypeAssessment:
					mutableReasons.add( new MutableSuperclassMutableTypeAssessment( type, mutableTypeAssessment ) );
					break;
				case ProvisoryTypeAssessment provisoryTypeAssessment:
					provisoryReasons.add( new ProvisorySuperclassProvisoryTypeAssessment( type, provisoryTypeAssessment ) );
					break;
				default:
					assert superclassAssessment instanceof ImmutableTypeAssessment || superclassAssessment instanceof UnderAssessmentTypeAssessment;
			}
		}

		for( Field field : type.getDeclaredFields() )
		{
			if( Modifier.isStatic( field.getModifiers() ) )
				continue;
			FieldAssessment fieldAssessment = fieldAssessor.assessField( field );
			switch( fieldAssessment )
			{
				case ProvisoryFieldTypeProvisoryFieldAssessment provisoryFieldAssessment:
					provisoryReasons.add( new ProvisoryFieldProvisoryTypeAssessment( type, provisoryFieldAssessment ) );
					break;
				case MutableFieldAssessment mutableFieldAssessment:
					mutableReasons.add( new MutableFieldMutableTypeAssessment( type, mutableFieldAssessment ) );
					break;
				default:
					assert fieldAssessment instanceof ImmutableFieldAssessment || fieldAssessment instanceof UnderAssessmentFieldAssessment;
			}
		}

		if( !mutableReasons.isEmpty() )
			return mutableReasons.size() == 1 ? mutableReasons.get( 0 ) : new MultiReasonMutableTypeAssessment( type, mutableReasons );

		if( !provisoryReasons.isEmpty() )
			return provisoryReasons.size() == 1 ? provisoryReasons.get( 0 ) : new MultiReasonProvisoryTypeAssessment( type, provisoryReasons );

		if( Helpers.isExtensible( type ) )
			return new ExtensibleProvisoryTypeAssessment( TypeAssessment.Mode.Assessed, type );

		return ImmutableTypeAssessment.instance;
	}

	private TypeAssessment assessSuperclass( Class<?> superclass )
	{
		TypeAssessment superclassAssessment = typeAssessor.assess( superclass );
		return switch( superclassAssessment )
			{
				//Cannot happen, because the superclass has obviously been extended, so it is extensible, so it can not be immutable.
				//DoNotCover
				case ImmutableTypeAssessment immutableTypeAssessment -> throw new AssertionError( immutableTypeAssessment );
				//Cannot happen, because the supertype of a class cannot be an interface.
				//DoNotCover
				case InterfaceProvisoryTypeAssessment interfaceAssessment -> throw new AssertionError( interfaceAssessment );
				case UnderAssessmentTypeAssessment underAssessmentTypeAssessment -> underAssessmentTypeAssessment;
				case MutableTypeAssessment mutableTypeAssessment -> mutableTypeAssessment;
				//This means that the supertype is immutable in all aspects except that it is extensible, so the supertype is not preventing us from being immutable.
				case ExtensibleProvisoryTypeAssessment ignore -> ImmutableTypeAssessment.instance;
				case ProvisoryTypeAssessment provisoryTypeAssessment -> provisoryTypeAssessment;
				//DoNotCover
				default -> throw new AssertionError( superclassAssessment );
			};
	}
}
