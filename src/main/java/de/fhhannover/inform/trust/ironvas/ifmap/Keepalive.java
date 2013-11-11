package de.fhhannover.inform.trust.ironvas.ifmap;

/*
 * #%L
 * ====================================================
 *   _____                _     ____  _____ _   _ _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \|  ___| | | | | | |
 *    | | | '__| | | / __| __|/ / _` | |_  | |_| | |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _| |  _  |  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_|   |_| |_|_| |_|
 *                             \____/
 * 
 * =====================================================
 * 
 * Fachhochschule Hannover 
 * (University of Applied Sciences and Arts, Hannover)
 * Faculty IV, Dept. of Computer Science
 * Ricklinger Stadtweg 118, 30459 Hannover, Germany
 * 
 * Email: trust@f4-i.fh-hannover.de
 * Website: http://trust.inform.fh-hannover.de/
 * 
 * This file is part of ironvas, version 0.1.1, implemented by the Trust@FHH 
 * research group at the Fachhochschule Hannover.
 * 
 * ironvas is a *highly experimental* integration of Open Vulnerability Assessment 
 * System (OpenVAS) into a MAP-Infrastructure. The integration aims to share security 
 * related informations (vulnerabilities detected by OpenVAS) with other network 
 * components in the TNC architecture via IF-MAP.
 * %%
 * Copyright (C) 2011 - 2013 Trust@FHH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.logging.Logger;

import de.fhhannover.inform.trust.ifmapj.channel.SSRC;
import de.fhhannover.inform.trust.ifmapj.exception.IfmapErrorResult;
import de.fhhannover.inform.trust.ifmapj.exception.IfmapException;

/**
 * A {@link Keepalive} can be used to keep an IF-MAP connection alive, by
 * continuously sending a re-new session request to the MAPS.
 *
 * @author Ralf Steuerwald
 *
 */
public class Keepalive implements Runnable {

    private static final Logger logger = Logger.getLogger(Keepalive.class
            .getName());

    private SSRC ssrc;

    /**
     * The interval between the renewSession requests in seconds. Must be
     * smaller than the session timeout value.
     */
    private int interval;

    /**
     * Creates a new {@link Keepalive} object which can be used to keep
     * the connection associated with the given {@link SSRC} alive. The
     * connection has to be established before.
     *
     * @param ssrc      the session to keep alive
     * @param interval  the interval in which re-new session request will be
     *                   send
     */
    public Keepalive(SSRC ssrc, int interval) {
        this.ssrc = ssrc;
        this.interval = interval;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                logger.fine("sending renewSession");
                ssrc.renewSession();
                Thread.sleep(interval * 1000);
            }
        } catch (IfmapException e) {
            logger.severe("renewSession failed: " + e.getMessage());
        } catch (IfmapErrorResult e) {
            logger.severe("renewSession failed: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("wakup by interrupt signal, exiting ...");
        } finally {
            try {
                ssrc.endSession();
            } catch (Exception e) {
                logger.warning("error while ending the session");
            }
            logger.info("shutdown complete.");
        }
    }
}
