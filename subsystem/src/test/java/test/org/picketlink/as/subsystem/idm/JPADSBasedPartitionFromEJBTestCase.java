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
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author pedroigor
 */
@RunWith(Arquillian.class)
public class JPADSBasedPartitionFromEJBTestCase {

    @Resource(mappedName = "picketlink/JPADSBasedPartitionManager")
    private PartitionManager jpaDSBasedPartitionManager;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = ShrinkWrap
                                        .create(WebArchive.class, "test.war")
                                        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                                        .addAsManifestResource(JPADSBasedPartitionFromEJBTestCase.class.getClassLoader()
                                            .getResource("deployment/jboss-deployment-structure-idm-simple-schema.xml"), "jboss-deployment-structure.xml")
                                        .addClass(JPADSBasedPartitionFromEJBTestCase.class)
                                        .addClass(IDMEJBInitializer.class);

        return deployment;
    }

    @Test
    public void testPartitionManager() throws Exception {
        IdentityManager identityManager = this.jpaDSBasedPartitionManager.createIdentityManager();

        User johny = BasicModel.getUser(identityManager, "johny");

        assertNotNull(johny);

        Password password = new Password("abcd1234");

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(johny.getLoginName(), password);

        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.VALID, credentials.getStatus());

        Role role = BasicModel.getRole(identityManager, "admin");

        assertNotNull(role);

        RelationshipManager relationshipManager = this.jpaDSBasedPartitionManager.createRelationshipManager();

        assertTrue(BasicModel.hasRole(relationshipManager, johny, role));
        assertNotNull(johny.getAttribute("testAttribute"));
        assertEquals("value", johny.getAttribute("testAttribute").getValue());
    }
}
