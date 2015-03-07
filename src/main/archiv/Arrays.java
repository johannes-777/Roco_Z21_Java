

//----------------------------------------------------------------------------------------
//	Copyright � 2006 - 2013 Tangible Software Solutions Inc.
//	This class can be used by anyone provided that the copyright notice remains intact.
//
//	This class provides the ability to initialize array elements with the default
//	constructions for the array type.
//----------------------------------------------------------------------------------------
public final class Arrays
{
	public static @byte[] initializeWithDefault@byteInstances(int length)
	{
		@byte[] array = new @byte[length];
		for (int i = 0; i < length; i++)
		{
			array[i] = new @byte();
		}
		return array;
	}

	public static TypeActIP[] initializeWithDefaultTypeActIPInstances(int length)
	{
		TypeActIP[] array = new TypeActIP[length];
		for (int i = 0; i < length; i++)
		{
			array[i] = new TypeActIP();
		}
		return array;
	}
}