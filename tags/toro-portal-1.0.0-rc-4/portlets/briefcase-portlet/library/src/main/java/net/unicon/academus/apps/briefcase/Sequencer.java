/*
 * Copyright (C) 2007 Unicon, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this distribution.  It is also available here:
 * http://www.fsf.org/licensing/licenses/gpl.html
 *
 * As a special exception to the terms and conditions of version
 * 2 of the GPL, you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications
 * as described in the GPL FLOSS exception.  You should have received
 * a copy of the text describing the FLOSS exception along with this
 * distribution.
 */

package net.unicon.academus.apps.briefcase;

/**
 * @author ibiswas
 *
 * This class is used to retrieve a sequence number.
 */
public final class Sequencer {

    private int seed;

    /**
     * Constructor
     * @param seed the start of the sequence
     */
    public Sequencer(int seed){

        // Assertions.
        if(seed < 0){
            throw new IllegalArgumentException("Seed value cannot be less than 0");
        }

        // Instance Members.
        this.seed = seed;

    }

    public Sequencer(){

        // Instance Members.
        this.seed = 0;

    }

    /**
     * returns the next number in the sequence
     * @return the next int number in the sequence
     */
    public synchronized int next(){
        return seed++;
    }
}
