package test.org.picketlink.as.subsystem.idm;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.module.entity.AttributeReferenceTypeEntity;
import org.picketlink.test.module.entity.AttributedTypeEntity;
import org.picketlink.test.module.entity.RelationshipIdentityTypeReferenceEntity;
import org.picketlink.test.module.entity.RelationshipTypeEntity;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author pedroigor
 */
@RunWith(Arquillian.class)
public class MixedLDAPAndJPABasedPartitionManagerTestCase {

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = ShrinkWrap
                                        .create(WebArchive.class, "test.war")
            .addAsResource(MixedLDAPAndJPABasedPartitionManagerTestCase.class.getClassLoader()
                .getResource("deployment/relationship-reference-persistence.xml"), "META-INF/persistence.xml")
                                        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                                        .addAsManifestResource(MixedLDAPAndJPABasedPartitionManagerTestCase.class.getClassLoader().getResource("deployment/jboss-deployment-structure-idm.xml"), "jboss-deployment-structure.xml")
                                        .addClass(MixedLDAPAndJPABasedPartitionManagerTestCase.class)
                                        .addClass(RelationshipIdentityTypeReferenceEntity.class)
                                        .addClass(RelationshipTypeEntity.class)
                                        .addClass(AttributedTypeEntity.class)
                                        .addClass(AttributeReferenceTypeEntity.class);

        return deployment;
    }

    @Resource(mappedName = "picketlink/MultipleStoreBasedPartitionManager")
    private PartitionManager partitionManager;

    @Test
    public void testPartitionManager() throws Exception {
        Realm defaultRealm = this.partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM);

        if (defaultRealm == null) {
            defaultRealm = new Realm(Realm.DEFAULT_REALM);

            this.partitionManager.add(defaultRealm);
        }

        IdentityManager identityManager = this.partitionManager.createIdentityManager();

        User user = new User("33j34u3444ca44553434");

        identityManager.add(user);

        Password password = new Password("3abcd1234");

        identityManager.updateCredential(user, password);

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user.getLoginName(), password);

        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.VALID, credentials.getStatus());

        Role role = new Role("3r334u444344l4er3434");

        identityManager.add(role);

        RelationshipManager relationshipManager = this.partitionManager.createRelationshipManager();

        BasicModel.grantRole(relationshipManager, user, role);

        user = BasicModel.getUser(identityManager, user.getLoginName());
        role = BasicModel.getRole(identityManager, role.getName());

        Thread.sleep(1000);

        assertTrue(BasicModel.hasRole(relationshipManager, user, role));

        user.setAttribute(new Attribute<String>("testAttribute", "value"));

        identityManager.update(user);

        Thread.sleep(1000);

        user = BasicModel.getUser(identityManager, user.getLoginName());

        assertNotNull(user.getAttribute("testAttribute"));
        assertEquals("value", user.getAttribute("testAttribute").getValue());
    }
}