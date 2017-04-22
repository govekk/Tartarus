
gpjpp - a Java package for genetic programming - version 1.0
------------------------------------------------------------
Copyright (c) 1997, Kim Kokkonen
See license.txt for warranty, use, and distribution information

Introduction
------------
gpjpp is a port to Java of the gpc++ kernel written by Adam Fraser and
Thomas Weinbrenner. Sincere thanks to these two for publishing their
source code for a clean object-oriented approach to genetic
programming.

Although gpjpp began as a straight port of gpc++, it has taken on a
life of its own. Here are some of the additions to gpc++ 0.52:

 - graphic output of expression trees using Java platform-
   independent gif files (and also graphic output of Lawnmower
   trails)

 - efficient diversity checking based on the Java Hashtable
   class, which allows this feature to be enabled with only a
   small performance penalty

 - Koza's greedy over-selection option for large populations

 - extensible GPRun class that encapsulates most details of a
   genetic programming test

 - more robust and efficient streaming code, with automatic
   checkpoint and restart built into the GPRun class

 - an explicit complexity limit that can be set on each GP

 - additional configuration variables to allow more testing without
   recompilation

If you are not familiar with gpc++, here is a list of its relevant
features, taken from Weinbrenner's readme file:

 - support for automatically defined functions (ADFs)

 - tournament and fitness proportionate selection

 - demetic grouping

 - optional steady state population

 - subtree crossover

 - swap and shrink mutation

Examples
--------
Included are four of Koza's standard examples: the artificial ant, the
hopping lawnmower, symbolic regression, and the boolean multiplexer.
All of the examples are configured by means of ini files, and all but
SymbReg allow the problem difficulty to be scaled via the ini file.

The examples are written as Java console applications. The library
could be used to develop Java applets, but I haven't done this myself.

Installing, compiling, and running
----------------------------------
Extract the zip file into any clean subdirectory. Be sure to respect
subdirectories and long file names when extracting.

Most of my work was done with Sun JDK 1.1 and Microsoft Visual J++ 1.1
(which actually uses JDK 1.0 language definition and base classes). I
tested both compilers under Windows 95. I also tested briefly with the
trial version of Symantec Visual Cafe, which also uses JDK 1.0
classes.

class files are not provided. You need to compile the gpjpp package
and the example programs before running them.

Each subdirectory under the base directory of the gpjpp installation
(gpjpp, Ant, Lawn, SymbReg, Mux) contains DOS batch files for
compiling (compile.bat) and running (run.bat) the relevant classes
using the Sun, Microsoft, or Symantec command line tools. Most of the
work of these scripts is done by batch files of the same name in the
base directory. You'll probably need to update the batch files for
your setup and platform. Add the command line option 'ms' (without the
quotes) to use the Microsoft tools or the option 'sj' to use the
Symantec tools. The Sun tools are used by default.

Each of the example subdirectories also includes one or more ini files
that configure the test runs. One of the ini files is specified as a
command line option in each subdirectory's run.bat.

The gpjpp library should be compatible with any Java compiler and
platform. I'd be interested to hear about any other platforms you use.

Performance
-----------
Performance of gpjpp is respectable, approaching that of gpc++ when
the Microsoft just-in-time compiler is used to execute the test
programs. Even so, gpjpp can be a memory hog. The Los Altos Trail ant
problem with 2000 individuals allocates almost 64 megabytes of memory,
which really thrashes my 32 megabyte Pentium machine. In this test
case, the Java garbage collector is dealing with roughly 1 million
memory blocks.

My results show that Sun's compiler generates faster code, but
Microsoft's JIT virtual machine runs the code faster, at least when
plenty of memory is available. For problems that push memory limits,
Sun's virtual machine seems faster. Based on limited testing, Symantec
seems to have the fastest compile and run times, even for large
problems.

The gpjpp code has not been optimized for network applications, where
splitting it into smaller but more numerous classes might be desirable
to lessen download or startup times. There are some good opportunities
for splitting out classes -- e.g., the fitness-based selection methods
could easily be separated.

The code has also not been tested in a multi-threaded application,
with the exception of standard garbage collection running in its own
low-priority thread. It would be nice to support operation in a
network of workstations, with each of them responsible for evaluating
parts of the population.

Error handling
--------------
gpjpp throws an exception whenever it encounters an error. Besides
standard Java exceptions such as IOException and IllegalAccessException,
it throws the generic RuntimeException type whenever gpjpp itself
detects and error. All of the RuntimeException errors are considered
to be non-recoverable programmer errors, so this branch of the Java
Throwable hierarchy was considered appropriate and no gpjpp-specific
exception subclass was created. The exception carries an informative
message. The GPRun class traps all Exception throws, and then prints
the message and a stack trace. All error detection, including null
pointer checking and array bounds checking, is always enabled.

Documentation
-------------
The gpjpp source code incorporates a complete reference manual ready
for processing by Sun's javadoc documentation generator. I decided not
to increase download time by including the html output from javadoc.
Run the makedocs.bat script after updating it for your platform and it
will create a set of html files describing the library. These are
output to the docs subdirectory. The standard Sun image files needed
by the html are provided in docs\images.

The GPVariables class includes a description of all the ini file
configuration parameters, since these are also fields within that
class.

The example programs are commented to show the actions needed to write
gpjpp-based applications of various types.

The documentation for Weinbrenner's gpc++ 0.52 library provides
additional useful background for the Java version.

Credits and contacts
--------------------
This package was developed as part of a project for Dr. Lewis J.
Pinson's Evolutionary Computation class at University of Colorado,
Colorado Springs.

The latest version of gpc++ can be found at
    http://www.emk.e-technik.th-darmstadt.de/~thomasw/gp.html

Dr. John Koza of Stanford University is the founder of the genetic
programming field. His web site is at
    http://www-cs-faculty.stanford.edu/~koza/index.html

gif files are written using freeware classes from Acme Labs. See
    http://www.acme.com
for additional information.

If you don't have javadoc, you can get it from Sun's site at
    http://www.javasoft.com
as part of the JDK download.

A 15-day trial version of Symantec Visual Cafe can be found at
    http://www.symantec.com

I'm a graduate student in Computer Science at UCCS. You can reach
me by e-mail at
    kimk@turbopower.com
The latest version of gpjpp can be found at
    http://www.turbopower.com/~kimk
