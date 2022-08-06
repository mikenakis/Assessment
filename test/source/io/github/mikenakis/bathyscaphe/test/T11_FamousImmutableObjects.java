/*
 * Copyright (c) 2022 Michael Belivanakis a.k.a. MikeNakis, michael.gr
 *
 * For licensing information, please see LICENSE.md.
 * You may not use this file except in compliance with the license.
 */

package io.github.mikenakis.bathyscaphe.test;

import io.github.mikenakis.bathyscaphe.Bathyscaphe;
import io.github.mikenakis.bathyscaphe.ObjectMustBeImmutableException;
import io.github.mikenakis.bathyscaphe.internal.mykit.MyKit;
import org.junit.Test;

import javax.swing.KeyStroke;
import java.io.File;
import java.lang.reflect.InaccessibleObjectException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Test.
 *
 * @author michael.gr
 */
public class T11_FamousImmutableObjects
{
	public T11_FamousImmutableObjects()
	{
		if( !MyKit.areAssertionsEnabled() )
			throw new AssertionError();
	}

	private static Inet4Address getInet4Address()
	{
		return MyTestKit.unchecked( () -> (Inet4Address)InetAddress.getByAddress( new byte[4] ) );
	}

	private static Inet6Address getInet6Address()
	{
		return MyTestKit.unchecked( () -> (Inet6Address)InetAddress.getByAddress( new byte[16] ) );
	}

	private static URL getUrl()
	{
		return MyTestKit.unchecked( () -> URI.create( "file:///" ).toURL() );
	}

	private static StackTraceElement getStackTraceElement()
	{
		try
		{
			throw new RuntimeException();
		}
		catch( RuntimeException e )
		{
			return e.getStackTrace()[0];
		}
	}

	@Test public void famous_immutable_objects_are_immutable()
	{
		//NOTE: The objects returned by OffsetDateTime.now(), OffsetTime.now(), and ZoneOffset.UTC used to be immutable in JDK 17, but are not anymore.
		List<Object> objects = List.of( new Object(), getClass(), false, 'c', (byte)1, (short)1, 1, 1L, 1.0f, 1.0, "", //
			Instant.EPOCH, Duration.ZERO, UUID.randomUUID(), LocalDate.EPOCH, LocalDateTime.now(), LocalTime.MIDNIGHT, MonthDay.now(), //
			Period.ZERO, Year.now(), YearMonth.now(), ZonedDateTime.now(), //
			new BigDecimal( 1 ), new BigInteger( "1" ), getInet4Address(), getInet6Address(), InetSocketAddress.createUnresolved( "", 0 ), //
			getClass().getDeclaredMethods()[0], getClass().getConstructors()[0], URI.create( "file:///" ), getUrl(), Locale.ROOT, //
			getStackTraceElement(), File.listRoots()[0] );
		for( Object object : objects )
			assert Bathyscaphe.objectMustBeImmutableAssertion( object );
	}

	@Test public void optional_is_mutable_or_immutable_depending_on_payload()
	{
		Bathyscaphe.objectMustBeImmutableAssertion( Optional.empty() );
		Bathyscaphe.objectMustBeImmutableAssertion( Optional.of( 1 ) );
		MyTestKit.expect( ObjectMustBeImmutableException.class, () -> Bathyscaphe.objectMustBeImmutableAssertion( Optional.of( new StringBuilder() ) ) );
	}

	@Test public void certain_classes_are_messed_up()
	{
		/* PEARL: ZoneId.systemDefault() returns an instance of ZoneRegion, which is mutable! And since ZoneRegion is jdk-internal, we cannot preassess it! */
		MyTestKit.expect( ObjectMustBeImmutableException.class, () -> Bathyscaphe.objectMustBeImmutableAssertion( ZoneId.systemDefault() ) );

		/* PEARL: Clock.systemUTC() returns an instance of SystemClock, which is inaccessible, so we cannot assess it! */
		MyTestKit.expect( InaccessibleObjectException.class, () -> Bathyscaphe.objectMustBeImmutableAssertion( Clock.systemUTC() ) );
	}

	@Test public void famous_mutable_objects_are_mutable()
	{
		List<Object> objects = List.of( new ArrayList<>(), new HashMap<>(), new Date(), new HashSet<>(), new LinkedList<>(), new Properties(), //
			new Random(), Pattern.compile( "" ).matcher( "" ), new StringBuilder(), new LinkedHashMap<>(), new LinkedHashSet<>(), //
			new SimpleDateFormat( "", Locale.ROOT ), new StringTokenizer( "" ), new ConcurrentHashMap<>(), KeyStroke.getKeyStroke( 'c' ) );
		for( Object object : objects )
			MyTestKit.expect( ObjectMustBeImmutableException.class, () -> Bathyscaphe.objectMustBeImmutableAssertion( object ) );
	}
}
