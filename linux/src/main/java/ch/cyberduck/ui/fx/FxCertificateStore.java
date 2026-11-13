package ch.cyberduck.ui.fx;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
import ch.cyberduck.core.CertificateTrustCallback;
import ch.cyberduck.core.DefaultCertificateStore;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * The core asks the store about every certificate chain a server sends. Chains that the Java runtime trusts (the
 * root certificates it ships) are accepted when the certificate is for the host; the user decides about the others,
 * instead of any certificate with a matching name being accepted.
 */
public class FxCertificateStore extends DefaultCertificateStore {
    private static final Logger log = LogManager.getLogger(FxCertificateStore.class);

    /**
     * The key exchange is not known here. The runtime accepts this value and checks the certificate for a signature
     * key, which every server certificate has.
     */
    private static final String AUTH_TYPE = "UNKNOWN";

    private final X509TrustManager runtime;
    private final DefaultHostnameVerifier verifier = new DefaultHostnameVerifier();

    public FxCertificateStore() {
        this(runtimeTrustManager());
    }

    FxCertificateStore(final X509TrustManager runtime) {
        this.runtime = runtime;
    }

    /**
     * @return The trust manager of the runtime with the root certificates it ships, or null if it cannot be made
     */
    static X509TrustManager runtimeTrustManager() {
        try {
            final TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            // Without a key store the factory loads the default trust store of the runtime
            factory.init((KeyStore) null);
            for(TrustManager manager : factory.getTrustManagers()) {
                if(manager instanceof X509TrustManager) {
                    return (X509TrustManager) manager;
                }
            }
            log.error("No X.509 trust manager in the runtime");
        }
        catch(NoSuchAlgorithmException | KeyStoreException e) {
            log.error("Failure loading the trust store of the runtime. {}", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean verify(final CertificateTrustCallback prompt, final String hostname, final List<X509Certificate> certificates) {
        if(certificates.isEmpty()) {
            return false;
        }
        if(this.trusted(hostname, certificates)) {
            log.debug("Certificate for {} trusted by the runtime", hostname);
            return true;
        }
        try {
            prompt.prompt(hostname, certificates);
            return true;
        }
        catch(ConnectionCanceledException e) {
            return false;
        }
    }

    /**
     * @return True if the runtime trusts the chain and the first certificate is for the host
     */
    boolean trusted(final String hostname, final List<X509Certificate> certificates) {
        if(null == runtime) {
            return false;
        }
        try {
            runtime.checkServerTrusted(certificates.toArray(new X509Certificate[0]), AUTH_TYPE);
            verifier.verify(hostname, certificates.get(0));
            return true;
        }
        catch(CertificateException | SSLException e) {
            log.info("Certificate for {} not trusted by the runtime. {}", hostname, e.getMessage());
            return false;
        }
    }
}
