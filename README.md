jcrfs [![Build Status](https://buildhive.cloudbees.com/job/nicoulaj/job/jcrfs/badge/icon)](https://buildhive.cloudbees.com/job/nicoulaj/job/jcrfs/)
=====

**A [filesystem in userspace](http://en.wikipedia.org/wiki/Filesystem_in_Userspace) for [Java Content Repositories](http://en.wikipedia.org/wiki/Content_repository_API_for_Java).**

jcrfs allows to mount a remote JCR repository as a local filesystem.

Status
------

**This software is alpha and is far from being fully functionnal. Do not use it on a production repository, it may cause data loss.**

TODO list:

* Complete implementation of the filesystem.
* Better validation of mount options.
* Handle other access methods than RMI.
* Handle other implementations than Jackrabbit.
* Handle users/groups/permissions mapping through a config file (ntfs-3G style).
* Remove dependency on Jackrabbit ?
* Unit/integration tests.

Usage
-----

### Prerequisites

The following software must be installed on your system:

 * [FUSE](http://fuse.sourceforge.net): should be available through your system package manager.
 * [fuse4j](https://github.com/dtrott/fuse4j): build from sources, and make sure the `javafs` command is available in `PATH` and `libjavafs.so` is installed.

### Installing

Grab a snapshot of the assembly [here](https://oss.sonatype.org/content/repositories/snapshots/net/ju-n/jcrfs/jcrfs), or build from sources:

    git clone git://github.com/nicoulaj/jcrfs.git
    mvn package

Extract the assembly and add the `bin/` folder to your `PATH`:

    tar xvzf jcrfs-VERSION.tar.gz
    export PATH=jcrfs-VERSION/bin:$PATH

### Using

To mount a remote JCR on a folder, you can now use the `mount.jcr` command. Here is an example:

    mkdir test
    mount.jcr test "http://localhost:8080/rmi" -f -o user=admin,pass=admin,worskpace=default

The following options are available:

 * `user`: the username used to connect to the JCR.
 * `pass`: the password used to connect to the JCR.
 * `workspace`: the JCR workspace to connect to.
