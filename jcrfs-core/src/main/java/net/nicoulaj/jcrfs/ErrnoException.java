/*
 * jcrfs, a filesystem in userspace for Java Content Repositories.
 * Copyright (C) 2012 Julien Nicoulaud <julien.nicoulaud@gmail.com>
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

/**
 * @author Julien Nicoulaud <julien.nicoulaud@gmail.com>
 */
public class ErrnoException extends Exception {
    protected int errno;

    public ErrnoException(int errno) {
        this.errno = errno;
    }

    public ErrnoException(String message, int errno) {
        super(message);
        this.errno = errno;
    }

    public ErrnoException(String message, Throwable cause, int errno) {
        super(message, cause);
        this.errno = errno;
    }

    public ErrnoException(Throwable cause, int errno) {
        super(cause);
        this.errno = errno;
    }

    public int getErrno() {
        return errno;
    }
}
