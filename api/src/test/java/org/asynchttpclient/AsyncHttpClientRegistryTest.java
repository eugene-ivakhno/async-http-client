/*
 * Copyright (c) 2010-2014 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.asynchttpclient;

import org.asynchttpclient.util.AsyncImplHelper;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import junit.extensions.PA;

public class AsyncHttpClientRegistryTest {

    private static final String TEST_AHC = "testAhc";

    @BeforeMethod
    public void setUp() {
        System.clearProperty(AsyncImplHelper.ASYNC_HTTP_CLIENT_REGISTRY_SYSTEM_PROPERTY);
        AsyncHttpClientRegistryImpl.getInstance().clearAllInstances();
        PA.setValue(AsyncHttpClientRegistryImpl.class, "_instance", null);
    }

    @BeforeClass
    public void setUpBeforeTest() {
        System.setProperty(AsyncImplHelper.ASYNC_HTTP_CLIENT_IMPL_SYSTEM_PROPERTY, "org.asynchttpclient.TestAsyncHttpClient");
    }

    @AfterClass
    public void tearDown() {
        System.clearProperty(AsyncImplHelper.ASYNC_HTTP_CLIENT_IMPL_SYSTEM_PROPERTY);
    }

    @Test(groups = "fast")
    public void testGetAndRegister() {
        AsyncHttpClient ahc = AsyncHttpClientFactory.getAsyncHttpClient();
        Assert.assertNull(AsyncHttpClientRegistryImpl.getInstance().get(TEST_AHC));
        Assert.assertNull(AsyncHttpClientRegistryImpl.getInstance().addOrReplace(TEST_AHC, ahc));
        Assert.assertNotNull(AsyncHttpClientRegistryImpl.getInstance().get(TEST_AHC));
    }

    @Test(groups = "fast")
    public void testDeRegister() {
        AsyncHttpClient ahc = AsyncHttpClientFactory.getAsyncHttpClient();
        Assert.assertFalse(AsyncHttpClientRegistryImpl.getInstance().unRegister(TEST_AHC));
        Assert.assertNull(AsyncHttpClientRegistryImpl.getInstance().addOrReplace(TEST_AHC, ahc));
        Assert.assertTrue(AsyncHttpClientRegistryImpl.getInstance().unRegister(TEST_AHC));
        Assert.assertNull(AsyncHttpClientRegistryImpl.getInstance().get(TEST_AHC));
    }

    @Test(groups = "fast")
    public void testRegisterIfNew() {
        AsyncHttpClient ahc = AsyncHttpClientFactory.getAsyncHttpClient();
        AsyncHttpClient ahc2 = AsyncHttpClientFactory.getAsyncHttpClient();
        Assert.assertNull(AsyncHttpClientRegistryImpl.getInstance().addOrReplace(TEST_AHC, ahc));
        Assert.assertFalse(AsyncHttpClientRegistryImpl.getInstance().registerIfNew(TEST_AHC, ahc2));
        Assert.assertTrue(AsyncHttpClientRegistryImpl.getInstance().get(TEST_AHC) == ahc);
        Assert.assertNotNull(AsyncHttpClientRegistryImpl.getInstance().addOrReplace(TEST_AHC, ahc2));
        Assert.assertTrue(AsyncHttpClientRegistryImpl.getInstance().get(TEST_AHC) == ahc2);
        Assert.assertTrue(AsyncHttpClientRegistryImpl.getInstance().registerIfNew(TEST_AHC + 1, ahc));
        Assert.assertTrue(AsyncHttpClientRegistryImpl.getInstance().get(TEST_AHC + 1) == ahc);
    }

    @Test(groups = "fast")
    public void testClearAllInstances() {
        AsyncHttpClient ahc = AsyncHttpClientFactory.getAsyncHttpClient();
        AsyncHttpClient ahc2 = AsyncHttpClientFactory.getAsyncHttpClient();
        AsyncHttpClient ahc3 = AsyncHttpClientFactory.getAsyncHttpClient();
        Assert.assertNull(AsyncHttpClientRegistryImpl.getInstance().addOrReplace(TEST_AHC, ahc));
        Assert.assertNull(AsyncHttpClientRegistryImpl.getInstance().addOrReplace(TEST_AHC + 2, ahc2));
        Assert.assertNull(AsyncHttpClientRegistryImpl.getInstance().addOrReplace(TEST_AHC + 3, ahc3));
        Assert.assertEquals(3, AsyncHttpClientRegistryImpl.getInstance().getAllRegisteredNames().size());
        AsyncHttpClientRegistryImpl.getInstance().clearAllInstances();
        Assert.assertEquals(0, AsyncHttpClientRegistryImpl.getInstance().getAllRegisteredNames().size());
        Assert.assertNull(AsyncHttpClientRegistryImpl.getInstance().get(TEST_AHC));
        Assert.assertNull(AsyncHttpClientRegistryImpl.getInstance().get(TEST_AHC + 2));
        Assert.assertNull(AsyncHttpClientRegistryImpl.getInstance().get(TEST_AHC + 3));
    }

    @Test(groups = "fast")
    public void testCustomAsyncHttpClientRegistry() {
        System.setProperty(AsyncImplHelper.ASYNC_HTTP_CLIENT_REGISTRY_SYSTEM_PROPERTY, TestAsyncHttpClientRegistry.class.getName());
        Assert.assertTrue(AsyncHttpClientRegistryImpl.getInstance() instanceof TestAsyncHttpClientRegistry);
    }

    @Test(groups = "fast", expectedExceptions = AsyncHttpClientImplException.class)
    public void testNonExistentAsyncHttpClientRegistry() {
        System.setProperty(AsyncImplHelper.ASYNC_HTTP_CLIENT_REGISTRY_SYSTEM_PROPERTY, "org.asynchttpclient.NonExistentAsyncRegistry");
        AsyncHttpClientRegistryImpl.getInstance();
        Assert.fail("Should never have reached here");
    }

    @Test(groups = "fast", expectedExceptions = AsyncHttpClientImplException.class)
    public void testBadAsyncHttpClientRegistry() {
        System.setProperty(AsyncImplHelper.ASYNC_HTTP_CLIENT_REGISTRY_SYSTEM_PROPERTY, "org.asynchttpclient.BadAsyncHttpClientRegistry");
        AsyncHttpClientRegistryImpl.getInstance();
        Assert.fail("Should never have reached here");
    }

}
