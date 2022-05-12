package mikenakis.assessment.internal.mykit.collections;

import java.util.LinkedHashMap;

public class IdentityLinkedHashSet<T> extends SetOnMap<T>
{
	public IdentityLinkedHashSet()
	{
		super( new LinkedHashMap<>() );
	}
}
