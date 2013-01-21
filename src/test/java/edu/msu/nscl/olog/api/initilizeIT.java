package edu.msu.nscl.olog.api;

import static edu.msu.nscl.olog.api.LogBuilder.log;
import static edu.msu.nscl.olog.api.LogbookBuilder.logbook;
import static edu.msu.nscl.olog.api.TagBuilder.tag;
import static edu.msu.nscl.olog.api.PropertyBuilder.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.msu.nscl.olog.api.OlogClientImpl.OlogClientBuilder;

public class initilizeIT {

	private static OlogClient client;

	// Property for all tests
	private static PropertyBuilder property;

	@Test
	public void createCSSProperty() throws Exception {
		client = OlogClientBuilder.serviceURL().withHTTPAuthentication(true)
				.create();
		 property =
		 property("Ticket").attribute("id").attribute("url");
		 client.set(property);
		 property =
		 property("Context").attribute("FileName").attribute("FileDescription");
		 client.set(property);
		//property = property("ShiftReport").attribute("Time")
		//		.attribute("ReportType").attribute("Operators")
		//		.attribute("Report");
		//client.set(property);
		//Collection<Log> result = client.findLogsBySearch("Scalability*");
		//System.out.println(result.size());
	}

}
