package com.analytics.JDO;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

public final class PMFSingleton {
private static final PersistenceManagerFactory pmfInstance= JDOHelper.getPersistenceManagerFactory("transactions");

public static PersistenceManagerFactory getPMF(){
	return pmfInstance;
}
}
