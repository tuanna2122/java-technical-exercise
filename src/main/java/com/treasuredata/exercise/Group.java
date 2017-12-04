package com.treasuredata.exercise;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread-safe container that stores a group ID and members.
 *
 * It can be added <tt>Member</tt> and return a member list as String. Also, it
 * can start and stop a background task that writes a member list to specified
 * files.
 *
 * This class is called a lot, so we need improve it.
 */
@ThreadSafe
public class Group {

	private static final Logger LOGGER = LoggerFactory.getLogger(Group.class);
	private static final String NEW_LINE = System.getProperty("line.separator");

	/**
	 * It is preferable to create a dummy private <tt>Object</tt> to use for
	 * <tt>synchronized</tt> block, so that it's reference can't be changed by any
	 * other code. For example if you have a setter method for <tt>Object</tt> on
	 * which you are synchronizing, it's reference can be changed by some other code
	 * leads to parallel execution of the synchronized block.
	 */
	private static final Object LOCK = new Object();

	private String groupId;
	private Set<Member> members;

	/**
	 * Using volatile keyword with <tt>shouldStop</tt> variable to make sure that
	 * every thread read its value from memory, not read from thread cache
	 */
	private volatile boolean shouldStop;

	public Group(String groupId) {
		this.groupId = groupId;
		this.members = new HashSet<>();
	}

	public void addMember(Member member) {
		if (null != member) {
			LOGGER.info("Adding new member to the group!");
			members.add(member);
		}
	}

	public String getMembersAsStringWith10xAge() {
		StringBuilder sb = new StringBuilder();
		for (Member member : members) {
			Integer age = member.getAge();
			// Don't ask the reason why `age` should be multiplied ;)
			age *= 10;
			sb.append(String.format("memberId=%s, age=%d¥n", member.getMemberId(), age));
			// This is just for the better format ;)
			sb.append(NEW_LINE);
		}
		return sb.toString();
	}

	/**
	 * Run a background task that writes a member list to specified files for 10
	 * times in background thread so that it doesn't block the caller's thread.
	 */
	public void startLoggingMemberList10Times(final String outputFilePrimary, final String outputFileSecondary) {
		startLoggingMemberListManyTimes(outputFilePrimary, outputFileSecondary, 10);
	}

	/**
	 * Run a background task that writes a member list to specified files
	 * indefinitely in background thread so that it doesn't block the caller's
	 * thread.
	 */
	public void startLoggingMemberList(final String outputFilePrimary, final String outputFileSecondary) {
		startLoggingMemberListManyTimes(outputFilePrimary, outputFileSecondary, -1);
	}

	/**
	 * Run a background task that writes a member list to specified files for many
	 * times in background thread so that it doesn't block the caller's thread.
	 */
	public void startLoggingMemberListManyTimes(final String outputFilePrimary, final String outputFileSecondary, final int count) {
		LOGGER.debug(String.format("Start writing the member lists for %d times!", count));

		MemberLoggingProcessor processor = new MemberLoggingProcessor(outputFilePrimary, outputFileSecondary, count);

		Thread t = new Thread(processor);
		t.start();
	}

	/**
	 * Stop the background task started by <tt>startLoggingMemberList()</tt> Of
	 * course, <tt>startLoggingMemberList</tt> can be called again after calling
	 * this method.
	 */
	public void stopLoggingMemberList() {
		shouldStop = true;
	}

    /**
     * Gets group's id
     * 
     * @return the id of the group
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Gets group's members
     * 
     * @return the members that belongs to the group
     */
    public Set<Member> getMembers() {
        return members;
    }

	/**
	 * The purpose of having this class is to separate the process of logging
	 * members into a new place so that it's readable and maintainable. Also, there
	 * should only be one background task at the same time
	 * 
	 * @author Tuan Nguyen
	 *
	 */
	private class MemberLoggingProcessor implements Runnable {

		private String outputFilePrimary;
		private String outputFileSecondary;
		private int count;

		private MemberLoggingProcessor(String outputFilePrimary, String outputFileSecondary, int count) {
			validate(outputFilePrimary, outputFileSecondary);

			this.outputFilePrimary = outputFilePrimary;
			this.outputFileSecondary = outputFileSecondary;
			this.count = count;
		}

		@Override
		public void run() {
			try (FileWriter fw1 = new FileWriter(outputFilePrimary, true);
					BufferedWriter bw1 = new BufferedWriter(fw1);
					FileWriter fw2 = new FileWriter(outputFileSecondary, true);
					BufferedWriter bw2 = new BufferedWriter(fw2)) {

				synchronized (LOCK) {
					if (count <= 0) {
						logMemberListIndefinitely(bw1, bw2);
					} else {
						logMemberListForManyTimes(bw1, bw2);
					}
				}

			} catch (Exception e) {
				// Write to the error.log so that you can trace back when your app go into production
				LOGGER.error("An error has occurred!", e);
				// You should pass the root cause of exception so that the caller can investigate
				throw new ApplicationException(String.format(
						"Unexpected error occurred. Please check these file names. outputFilePrimary=%s, outputFileSecondary=%s",
						outputFilePrimary, outputFileSecondary), e);
			}
		}

		/**
		 * This method is for validating the mandatory fields
		 * 
		 * @param outputFilePrimary the path to primary file
		 * @param outputFileSecondary the path to secondary field
		 */
		private void validate(String outputFilePrimary, String outputFileSecondary) {
			if (null == outputFilePrimary || null == outputFileSecondary) {
				throw new ApplicationException("You must specify both two target files");
			}

			File primaryFile = new File(outputFilePrimary);
			File secondaryFile = new File(outputFileSecondary);
			if (!primaryFile.exists() || !secondaryFile.exists()) {
				throw new ApplicationException("Both two target files must be exist");
			}
		}

		/**
		 * This method is to write a member list to specified files
		 * 
		 * @param bw1 the {@link BufferedWriter} for primary file
		 * @param bw2 the {@link BufferedWriter} for secondary file
		 * @throws Exception if any errors occur when writing files
		 */
		private void logMemberList(BufferedWriter bw1, BufferedWriter bw2) throws Exception {
			String content = getMembersAsStringWith10xAge();

			bw1.write(content);
			bw2.write(content);

			// Calls flush() method so the content will appear on the target files
			bw1.flush();
			bw2.flush();
		}

		/**
		 * This method will log a member list for N times<tt>(N = count)</tt>
		 * 
		 * @param bw1 the {@link BufferedWriter} for primary file
		 * @param bw2 the {@link BufferedWriter} for secondary file
		 * @throws Exception if any errors occur when writing files
		 */
		private void logMemberListForManyTimes(BufferedWriter bw1, BufferedWriter bw2) throws Exception {
			for (int i = 0; i < count; i++) {
				if (shouldStop) {
					LOGGER.info("Stop writing the member lists!");
					break;
				}

				logMemberList(bw1, bw2);

				try {
					// Using TimeUnit class to pause a thread is better than Thread.sleep() method
					// because it improves readability. You know up front that whether thread is
					// stopping for 1 millisecond or 1 second, which was not visible in case of
					// Thread.sleep()
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					LOGGER.error("An error has occurred!", e);
				}
			}
		}

		/**
		 * This method will indefinitely log a member list until
		 * stopPrintingMemberList() is called
		 * 
		 * @param bw1 the {@link BufferedWriter} for primary file
		 * @param bw2 the {@link BufferedWriter} for secondary file
		 * @throws Exception if any errors occur when writing files
		 */
		private void logMemberListIndefinitely(BufferedWriter bw1, BufferedWriter bw2) throws Exception {
			while (!shouldStop) {
				logMemberList(bw1, bw2);

				try {
					// Using TimeUnit class to pause a thread is better than Thread.sleep() method
					// because it improves readability. You know up front that whether thread is
					// stopping for 1 millisecond or 1 second, which was not visible in case of
					// Thread.sleep()
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					LOGGER.error("An error has occurred!", e);
				}
			}
		}

	}
}