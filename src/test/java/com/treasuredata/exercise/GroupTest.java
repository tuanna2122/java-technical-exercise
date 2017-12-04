/**
 * Test class for Group class
 */
package com.treasuredata.exercise;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tuan Nguyen
 *
 */
public class GroupTest {

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testLoggingMemberList() {
		final String outputFilePrimary = getClass().getClassLoader().getResource("test-data1.txt").getPath();
		final String outputFileSecondary = getClass().getClassLoader().getResource("test-data2.txt").getPath();

		Group group1 = new Group("group1");
		Group group2 = new Group("group2");

		group1.addMember(new Member("m1", 18));
		group2.addMember(new Member("m2", 19));

		// These will be sequentially executed
		group1.startLoggingMemberList10Times(outputFilePrimary, outputFileSecondary);
		group2.startLoggingMemberList10Times(outputFilePrimary, outputFileSecondary);
	}
}
