/*
 * jcrfs, a filesystem in userspace (FUSE) for Java Content Repositories (JCR).
 * Copyright (C) 2011-2012 Julien Nicoulaud <julien.nicoulaud@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.nicoulaj.jcrfs;

import fuse.*;
import org.apache.jackrabbit.rmi.repository.URLRemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * Filesystem implementation that connects to a remote Java Content Repository.
 * <p/>
 * TODO Implement {@link fuse.XattrSupport}.
 *
 * @author Julien Nicoulaud <julien.nicoulaud@gmail.com>
 */
@SuppressWarnings({"OctalInteger"})
public class JcrFileSystem implements Filesystem3, LifecycleSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(JcrFileSystem.class);

    public static final String OPTION_USER = "user";

    public static final String OPTION_PASS = "pass";

    public static final String OPTION_WORKSPACE = "workspace";

    private static final int NAME_LENGTH = 1024;

    public static final int DIR_MODE = 0755;

    public static final int FILE_MODE = 0644;

    protected Session session;

    public JcrFileSystem(String[] args) {
        final JcrfsArgumentParser argsParser = new JcrfsArgumentParser(args);
        LOGGER.debug("Mount point: {}", argsParser.getMountPoint());
        LOGGER.debug("Source: {}", argsParser.getSource());
        LOGGER.debug("Foreground: {}", argsParser.isForeground());
        LOGGER.debug("Options: {}", argsParser.getOptions());

        try {
            final Repository repository = new URLRemoteRepository(argsParser.getSource());
            final Credentials credentials = new SimpleCredentials(argsParser.getOption(OPTION_USER), argsParser.getOption(OPTION_PASS).toCharArray());
            session = repository.login(credentials, argsParser.getOption(OPTION_WORKSPACE));
        } catch (MalformedURLException e) {
            LOGGER.error("Invalid JCR repository URL", e);
        } catch (LoginException e) {
            LOGGER.error("Login failed", e);
        } catch (RepositoryException e) {
            LOGGER.error("Repository error occured", e);
        }
    }

    public int init() {
        LOGGER.info("Initializing JCR filesystem");
        return 0;
    }

    public int destroy() {
        LOGGER.info("Destroying JCR filesystem");
        return 0;
    }

    public int getattr(String path, FuseGetattrSetter getattrSetter) throws FuseException {
        final Item item;
        item = lookup(path);

        int time = (int) (System.currentTimeMillis() / 1000L);
        try {
            if (item instanceof Node) {
                final Node node = (Node) item;
                getattrSetter.set(item.hashCode(), // FIXME Use identifier ?
                                  FuseFtypeConstants.TYPE_DIR | DIR_MODE, // FIXME
                                  1, // FIXME
                                  0, // FIXME
                                  0, // FIXME
                                  0, // FIXME
                                  node.getNodes().getSize() * NAME_LENGTH, // FIXME
                                  0, // N/A
                                  time, // FIXME
                                  time, // FIXME
                                  time); // FIXME
                return 0;
            } else if (item instanceof Property) {
                final Property property = (Property) item;
                final long propSize = getPropertySize(property);
                getattrSetter.set(property.hashCode(),
                                  FuseFtypeConstants.TYPE_FILE | FILE_MODE, // FIXME
                                  1, // FIXME
                                  0, // FIXME
                                  0, // FIXME
                                  0, // FIXME
                                  propSize,  // FIXME
                                  0, // N/A
                                  time, // FIXME
                                  time, // FIXME
                                  time); // FIXME
                return 0;
            }
        } catch (RepositoryException e) {
            LOGGER.error("Failed resolving attributes for item at path '{}'", path, e);
            return Errno.EREMOTEIO;
        }

        return Errno.EREMOTEIO;
    }

    public int readlink(String path, CharBuffer link) throws FuseException {
        return Errno.ENOTSUPP;
    }

    public int getdir(String path, FuseDirFiller dirFiller) throws FuseException {
        final Item item;
        item = lookup(path);

        try {
            if (item instanceof Node) {
                final Node node = (Node) item;
                final NodeIterator nit = node.getNodes();
                while (nit.hasNext()) {
                    Node child = nit.nextNode();
                    dirFiller.add(child.getName(), child.hashCode(), FuseFtypeConstants.TYPE_DIR | DIR_MODE);
                }

                final PropertyIterator pit = node.getProperties();
                while (pit.hasNext()) {
                    Property prop = pit.nextProperty();
                    dirFiller.add(prop.getName(), prop.hashCode(), FuseFtypeConstants.TYPE_FILE | FILE_MODE);
                }

                return 0;
            }

        } catch (RepositoryException e) {
            LOGGER.error("Failed resolving children for item at path '{}'", path, e);
            return Errno.EREMOTEIO;
        }

        return Errno.ENOTDIR;
    }

    public int mknod(String path, int mode, int rdev) throws FuseException {
        return Errno.ENOSYS; // TODO Not implemented
    }

    public int mkdir(String path, int mode) throws FuseException {
        return Errno.ENOSYS; // TODO Not implemented
    }

    public int unlink(String path) throws FuseException {
        return Errno.ENOTSUPP;
    }

    public int rmdir(String path) throws FuseException {
        return Errno.ENOSYS; // TODO Not implemented
    }

    public int symlink(String from, String to) throws FuseException {
        return Errno.ENOTSUPP;
    }

    public int rename(String from, String to) throws FuseException {
        return Errno.ENOSYS; // TODO Not implemented
    }

    public int link(String from, String to) throws FuseException {
        return Errno.ENOTSUPP;
    }

    public int chmod(String path, int mode) throws FuseException {
        return Errno.ENOSYS; // TODO Not implemented
    }

    public int chown(String path, int uid, int gid) throws FuseException {
        return Errno.ENOSYS; // TODO Not implemented
    }

    public int truncate(String path, long size) throws FuseException {
        return Errno.ENOSYS; // TODO Not implemented
    }

    public int utime(String path, int atime, int mtime) throws FuseException {
        return Errno.ENOSYS; // TODO Not implemented
    }

    public int statfs(FuseStatfsSetter statfsSetter) throws FuseException {
        return Errno.ENOTSUPP;
    }

    public int open(String path, int flags, FuseOpenSetter openSetter) throws FuseException {
        final Item item;
        item = lookup(path);

        if (item != null) {
            openSetter.setFh(item);
            return 0;
        }

        return Errno.ENOENT;
    }

    public int read(String path, Object fh, ByteBuffer buf, long offset) throws FuseException {
        // FIXME Quick hack!
        if (fh instanceof Property) {
            Property prop = (Property) fh;
            try {
                if (!prop.isMultiple()) {
                    InputStream in = null;
                    in = prop.getBinary().getStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream((int) prop.getBinary().getSize());
                    byte[] tmp = new byte[(int) prop.getBinary().getSize()];
                    while (true) {
                        int r = 0;
                        try {
                            r = in.read(tmp);
                        } catch (IOException e) {
                            LOGGER.error("Failed reading property value");
                            return Errno.EREMOTEIO;
                        }
                        if (r == -1) break;
                        out.write(tmp, 0, r);
                    }
                    buf.put(out.toByteArray(), (int) offset, tmp.length);
                    return 0;
                } else {
                    return Errno.ENOSYS; // TODO Not implemented
                }
            } catch (RepositoryException e) {
                LOGGER.error("Repository error occured", e);
                return Errno.EREMOTEIO;
            }
        }
        return Errno.EBADF;
    }

    public int write(String path, Object fh, boolean isWritepage, ByteBuffer buf, long offset) throws FuseException {
        return Errno.ENOSYS; // TODO Not implemented
    }

    public int flush(String path, Object fh) throws FuseException {
        return Errno.ENOSYS; // TODO Not implemented
    }

    public int release(String path, Object fh, int flags) throws FuseException {
        return Errno.ENOSYS; // TODO Not implemented
    }

    public int fsync(String path, Object fh, boolean isDatasync) throws FuseException {
        return Errno.ENOSYS; // TODO Not implemented
    }

    private Item lookup(String path) throws FuseException {
        try {
            return session.getItem(path);
        } catch (PathNotFoundException e) {
            throw new FuseException("No item found at path '" + path + "'", e).initErrno(Errno.ENOENT);
        } catch (RepositoryException e) {
            throw new FuseException("Failed looking up item at path '" + path + "'", e).initErrno(Errno.EREMOTEIO);
        }
    }

    public static long getPropertySize(final Property property) {
        long res = 0;
        try {
            if (property.isMultiple()) {
                final long[] lengths = property.getLengths();
                for (long length : lengths) res += length;
            } else {
                res = property.getLength();
            }
        } catch (RepositoryException e) {
            LOGGER.error("Failed calculating property size '{}'", property, e);
        }
        return res;
    }
}
