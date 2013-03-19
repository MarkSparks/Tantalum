/*
 Copyright (c) 2013, Paul Houghton and Futurice Oy
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package org.tantalum;

import java.io.IOException;
import java.util.Vector;
import org.tantalum.storage.FlashCache;
import org.tantalum.storage.ImageTypeHandler;
import org.tantalum.util.L;

/**
 * For each platform we support (J2ME, Android, ..) there is an implementation
 * of PlatformAdapter which hooks to the appropriate platform code.
 * 
 * Note that by design this is a very minimal set of functions that provide
 * object persistence, logging, creating an image, UI thread access and
 * network access. Networking differences require one additional adapter,
 * HttpConn.
 * 
 * The easiest way to add support for an additional platforms is copy one
 * of the existing adapters and, well, adapt.
 * 
 * @author phou
 */
public interface PlatformAdapter {

    /**
     * Execute the run() method of the action on the platform's user interface
     * thread. The object will be queued by the platform and run soon, after
     * previously queued incoming events and tasks.
     *
     * Beware that calling this too frequently may make your application user
     * interface laggy (response after a long time). If your interface responds
     * slowly, check first that you are not doing slow processes on the UI
     * thread, then try to reduce the number of calls to this method.
     *
     * Unfortunately there is no visibility to how large the user interface
     * event queue has grown so this warning can not be automated.
     *
     * @param action
     */
    public void doRunOnUiThread(final Runnable action);

    /**
     * The application calls this when all Workers have completed shutdown to
     * inform the phone that the program has closed itself.
     *
     * Do not call this directly, call Worker.shutdown() to initiate a close
     *
     */
    public void doNotifyDestroyed();

    /**
     * Get a platform-specific logging class. Add Tantalum logs to the
     * platform's own logs allows you to use filters, redirect from remote
     * device, and any any similar features as for example on Android. On
     * J2ME we provide USB debugging in a terminal on your PC.
     * 
     * @return 
     */
    public L doGetLog();

    /**
     * Provide a return the converts from common Internet compressed byte[]
     * formats such as PNG and JPG into the platform's native Image object.
     * 
     * Note that if you are using a UI framework such as LWUIT that replaces
     * the native image format with it's own wrapper, you should implement
     * that in your app. There is an example of this in the Tantalum
     * LWUIT BBC Reader app.
     * 
     * @return 
     */
    public ImageTypeHandler doGetImageTypeHandler();

    /**
     * Get a persistence support class. Each cache is identified by a
     * single character that is both the name of that cache and the
     * priority. These are guaranteed by runtime exceptions at cache creation
     * time to be globally unique within each application.
     * 
     * @param priority
     * @return 
     */
    public FlashCache doGetFlashCache(char priority);

    /**
     * Create an HTTP PUT connection appropriate for this phone platform
     *
     * @param url
     * @param requestPropertyKeys
     * @param requestPropertyValues
     * @param bytes
     * @param requestMethod
     * @return
     * @throws IOException
     */
    public PlatformUtils.HttpConn doGetHttpConn(final String url, final Vector requestPropertyKeys, final Vector requestPropertyValues, final byte[] bytes, final String requestMethod) throws IOException;
}
