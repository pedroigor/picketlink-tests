package org.picketlink.test.idm;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.User;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.picketlink.idm.credential.Credentials.Status;

/**
 * @author pedroigor
 */
@RunWith(Arquillian.class)
public class IdentityConfigurationProducerFromModulesTestCase {

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = ShrinkWrap
                                        .create(WebArchive.class, "test.war")
                                        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                                        .addAsManifestResource(new StringAsset("Dependencies: org.picketlink.core meta-inf,org.picketlink.core.api meta-inf,org.picketlink.idm.api meta-inf\n"), "MANIFEST.MF")
                                        .addClass(IdentityConfigurationProducerFromModulesTestCase.class).addClass(MyIdentityConfigurationProducer.class);

        return deployment;
    }

    @Inject
    private PartitionManager partitionManager;

    @Test
    public void testPartitionManager() throws Exception {
        Realm defaultRealm = this.partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM);

        if (defaultRealm != null) {
            this.partitionManager.remove(defaultRealm);
        }

        defaultRealm = new Realm(Realm.DEFAULT_REALM);

        this.partitionManager.add(defaultRealm);

        IdentityManager identityManager = this.partitionManager.createIdentityManager();

        User user = new User("johny");

        identityManager.add(user);

        Password password = new Password("abcd1234");

        identityManager.updateCredential(user, password);

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user.getLoginName(), password);

        identityManager.validateCredentials(credentials);

        assertEquals(Status.VALID, credentials.getStatus());
    }

    @ApplicationScoped
    public static class MyIdentityConfigurationProducer {

        @Produces
        public IdentityConfiguration produceIdentityConfiguration() {
            return new IdentityConfigurationBuilder().named("default").stores().file().supportAllFeatures().build();
        }
    }
}
