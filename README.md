# Java Technical Exercise - Tuan Nguyen

**The ```PoorGroup.java``` has been renamed to ```Group.java``` and now placed in ```src/main/java/com/treasuredata/exercise```**

Please notice:

* This is a maven base project. So, you can import this into any IDE that you like (Eclipse, Intellij IDEA, etc...)
* This requires **Java 7 or later** installed in your machine. Please don't try with Java 6 or lower

### Please see the following for what I've changed and why do I do it

1. **Member** should be placed in separate class since if you nested it in PoorGroup class then its instances must qualify the allocation with an enclosing instance of type PoorGroupUpdated (e.g. x.new A() where x is an instance of PoorGroup). This is for improving readability, maintainability and extensibility
2. Added public access modifier for constructor of **Member** class so that it can be accessed from outside of the package
3. Updated **equals()** method of **Member** class to do the following things:
	- You should have a check for the **Object** argument to avoid NullPointerException
	- You should have a check for ```member.memberId``` because it's nullable
	- You shouldn't use ```==``` operator, instead of that you use **equals()** method so that it will compare the values of 2 strings
	- When you have overridden equals() method, the hashCode() method should be available also as Java standard convention
4. Updated **Member** class
	- The **memberId** and **age** attributes should be private access so that these can't be modified from outside. Let's imagine that when a thread is accessing/printing a member object's memberId attribute and someone try to update it by doing so: ```member.memberId="something"```
5. Updated **PoorGroup** class to do the following things:
	- **PoorGroup** class level variables such as: **groupId**, **members**, **shouldStop**. These should be private access rather than default access modifier as it is since when a thread is running, another thread can modify the state of the object by changing its attributes: ```poorGroup.groupId="something"; poorGroup.members=something```
	- Added getter methods for both **groupId** and **members** so that you can read these from outside
	- Updated **getMembersAsStringWith10xAge()** method since you shouldn't use string concatenation, instead of that you should use **StringBuilder**
	- Updated **startLoggingMemberList10Times()** method to do the following things:
		- Since you have multiple calls to **write()** method of **FileWriter**. So, instead of using FileWriter you should use **BufferedWriter** for better performance and more efficient. The main purpose of having BufferedWriter is because of without a BufferedWriter this could make **10 system calls** and writes to disk which is inefficient. With a BufferedWriter, these can all be buffered together and as the **default buffer size is 8192 characters** this become just **1 system call** to write
		- You should have **just one instance of FileWriter** for writing content to the files instead of creating a new one each time the loop (while) run. It's really bad and can cause performance issues
		- You should utilize **try-with-resources** feature which is available from Java 7 so that you can avoid the boiler-plate code (finally block, close resources manually) and automatically resource management
	- Added logging so that you can trace back when your app go into production
	- Updated **addMember()** method to avoid NullPointerException
	- Renamed **PoorGroup** class to **Group** as it's better now, I think :+1:
	- Added private inner class **MemberLoggingProcessor** to separate the process of logging members into a new place so that it's readable and maintainable. Also, there should only be one background task at the same time. Let's imagine that there will be more than one threads that are writing a file at the same time, this will cause data inconsistency issue. Hence, the process of logging member list should be sequentially
	- Using ```volatile``` keyword with **shouldStop** variable to make sure that every thread read its value from memory, not read from thread cache
