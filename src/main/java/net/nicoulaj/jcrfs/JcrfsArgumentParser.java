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

import fuse.util.FuseArgumentParser;

import java.util.HashMap;
import java.util.Map;

/**
 * An improved {@link FuseArgumentParser} that handles mount options parsing.
 *
 * @author Julien Nicoulaud <julien.nicoulaud@gmail.com>
 */
public class JcrfsArgumentParser extends FuseArgumentParser {
    
    protected Map<String, String> options = new HashMap<String, String>();
    
    public JcrfsArgumentParser(String[] args) {
        super(args);
        for (String remainingArg : getRemaining()) {
            if (!"-o".equals(remainingArg)) {
                final String[] opts = remainingArg.split(",");
                for (int i = 0; i < opts.length; i++) {
                    final String[] keyValue = opts[i].split("=", 2);
                    final String value = (keyValue.length > 1) ? keyValue[1] : null;
                    options.put(keyValue[0], value);
                }
            }
        }
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public String getOption(String option) {
        return options.get(option);
    }

    public boolean hasOption(String option) {
        return options.containsKey(option);
    }
}
