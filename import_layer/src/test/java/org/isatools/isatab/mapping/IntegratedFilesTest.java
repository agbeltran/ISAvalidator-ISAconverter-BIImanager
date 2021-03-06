/*
 * __________
 * CREDITS
 * __________
 *
 * Team page: http://isatab.sf.net/
 * - Marco Brandizi (software engineer: ISAvalidator, ISAconverter, BII data management utility, BII model)
 * - Eamonn Maguire (software engineer: ISAcreator, ISAcreator configurator, ISAvalidator, ISAconverter,  BII data management utility, BII web)
 * - Nataliya Sklyar (software engineer: BII web application, BII model,  BII data management utility)
 * - Philippe Rocca-Serra (technical coordinator: user requirements and standards compliance for ISA software, ISA-tab format specification, BII model, ISAcreator wizard, ontology)
 * - Susanna-Assunta Sansone (coordinator: ISA infrastructure design, standards compliance, ISA-tab format specification, BII model, funds raising)
 *
 * Contributors:
 * - Manon Delahaye (ISA team trainee: BII web services)
 * - Richard Evans (ISA team trainee: rISAtab)
 *
 *
 * ______________________
 * Contacts and Feedback:
 * ______________________
 *
 * Project overview: http://isatab.sourceforge.net/
 *
 * To follow general discussion: isatab-devel@list.sourceforge.net
 * To contact the developers: isatools@googlegroups.com
 *
 * To report bugs: http://sourceforge.net/tracker/?group_id=215183&atid=1032649
 * To request enhancements: �http://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *
 * __________
 * License:
 * __________
 *
 * Reciprocal Public License 1.5 (RPL1.5)
 * [OSI Approved License]
 *
 * Reciprocal Public License (RPL)
 * Version 1.5, July 15, 2007
 * Copyright (C) 2001-2007
 * Technical Pursuit Inc.,
 * All Rights Reserved.
 *
 * http://www.opensource.org/licenses/rpl1.5.txt
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */

package org.isatools.isatab.mapping;

import org.isatools.tablib.mapping.SectionTabMapper;
import org.isatools.tablib.mapping.testModels.isatab.MaFormatTabMapper;
import org.isatools.tablib.parser.TabLoader;
import org.isatools.tablib.schema.*;
import org.isatools.tablib.utils.BIIObjectStore;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Maps an example of a whole format set, with the ISATAB references and investigation sections.
 * Feb 27, 2008
 *
 * @author brandizi
 */
public class IntegratedFilesTest {

	@Test
	public void testIntegratedFiles() throws Exception {
		// Load the test case, as usually
		//
		out.println("\n\n_____ Testing Integrated Files _______");
		InputStream input = new BufferedInputStream(this.getClass().getResourceAsStream(
				"/test-data/isatab/integratedFiles/study_v2.xml")
		);
		FormatSet schema = SchemaBuilder.loadFormatSetFromXML(input);
		assertNotNull("I didn't get a schema", schema);

		TabLoader loader = new TabLoader(schema);

		FormatInstance formatInstance = loader.parse(
				null,
				new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(
						"/test-data/isatab/integratedFiles/study_v2.csv"))),
				"investigation"
		);

		Map<String, String> filesMap = new HashMap<String, String>();
		filesMap.put("assay_tx.csv", "transcriptomics_assay");
		FormatSetInstance formatSetInstance = loader.loadResourcesFromFileIds(
				"/test-data/isatab/integratedFiles/", filesMap
		);

		SectionInstance ontoSourcesInstance = formatInstance.getSectionInstance("ontoSources");
		List<Record> records = ontoSourcesInstance.getRecords();
		assertNotNull("I couldn't get records for onto-sources!", records);
		assertTrue("I couldn't get records for onto-sources (empty result)!", records.size() > 0);

		SectionInstance firstStudyInstance = formatInstance.getSectionInstance("study");
		records = firstStudyInstance.getRecords();
		assertNotNull("I couldn't get records for studies!", records);
		assertTrue("I couldn't get records for studies (empty result)!", records.size() > 0);

		FormatInstance assayFileInstance = formatSetInstance.getFormatInstance("transcriptomics_assay");

		// Invoke the mappers
		//
		BIIObjectStore store = new BIIObjectStore();

		SectionTabMapper mapper = new OntologySourceTabMapper(store, ontoSourcesInstance);
		mapper.map();

		mapper = new StudyBlockTabMapper(store, firstStudyInstance);
		mapper.map();

		MaFormatTabMapper assayMapper = new MaFormatTabMapper(store, assayFileInstance);
		assayMapper.map();


		out.println("___ RESULTS: ___");
		out.println(store.toStringVerbose());

		out.println("_____ /end: Testing Integrated Files _______\n\n\n");
	}

}