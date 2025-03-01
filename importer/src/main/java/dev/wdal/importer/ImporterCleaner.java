package dev.wdal.importer;

import java.lang.ref.Cleaner;

public abstract class ImporterCleaner {
    private static final Cleaner cleaner = Cleaner.create();
	public static Cleaner getCleaner()
	{
		return cleaner;
	}
}
