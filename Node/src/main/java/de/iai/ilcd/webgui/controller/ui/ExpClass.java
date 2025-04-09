package de.iai.ilcd.webgui.controller.ui;

import de.iai.ilcd.model.lifecyclemodel.LifeCycleModel;
import de.iai.ilcd.persistence.PersistenceUtil;
import eu.europa.ec.jrc.lca.commons.security.encryption.KeysGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.io.*;
import java.math.BigInteger;
import java.security.spec.RSAPublicKeySpec;

@Named
@Deprecated
public class ExpClass {

    private static Logger logger = LogManager.getLogger(ExpClass.class);

    public String erd = "123";

    public void doStuff() {
        erd = "789";
        logger.warn("Button has been clicked");

        genKey();
//
//		LifeCycleModel lcm = new LifeCycleModel();
//		LifeCycleModelDao dao = new LifeCycleModelDao();
//
//		lcm = dao.getByUuid("20062015-184a-41b8-8fa6-49e999cbd101");
//
//
//		Set<DataSet> deps = dao.getDependencies(lcm, DependenciesMode.ALL);

//		System.out.println(deps);


//		
//		
//		lcm.setUuid(new Uuid("ec389206-53dd-48bc-8f21-4e0669077300"));
//		try {
//			lcm.setVersion(new DataSetVersion(1, 3, 4));
//		} catch (FormatException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		lcm.setImportDate(new Date(System.currentTimeMillis()));\\

//		save(lcm);
    }

    public LifeCycleModel find(String UUID, int Major) {
        EntityManager em = PersistenceUtil.getEntityManager();
        Query q = em.createQuery(
                "SELECT lcm FROM LifeCycleModel lcm WHERE lcm.uuid.uuid = :uuid AND lcm.version.majorVersion = :ver",
                LifeCycleModel.class);
        q.setParameter("uuid", UUID);
        q.setParameter("ver", Major);
        q.setMaxResults(1);
        return (LifeCycleModel) q.getSingleResult();
    }

    public void save(Object obj) {
        EntityManager em = PersistenceUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(obj);
        tx.commit();
    }

    public void genKey() {
//		storePublic(KeysGenerator.getPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxvBAKFo2rRRjktU/Zl4dSmwV2Wwq0Ts664R7o0xMnEn7366cuNyPf6JlrAPZxXcXFmpER7RBpvxpJ4l+lI3tk3IGnLzCqwUdp7/jwqv30XdSb0/XNA/VJwi8x0Yx+8cDzdT3s8+aU0cCEF217S3DZzH/rDlj76aY1sX/z9uItlB99AkOm1HptpRT1DL1ulRcO4DO0CNNzYGoBMX2dhJ9RfUUJWyhVwa1+jc49IbELSckfefuuBeY7r8mgwlk+4nILMdQrlZTD5R9nJWDlzcgUb7xR1iMYCyTm7OJ+NZs12JR5b4UPMGp9vPDWoVjI+/RJzStk/vH/xKy+QwcE1auyQIDAQAB"));
        storePublic(KeysGenerator.getPublicKey("s50Oe6-3Kf4wFZ7O-usp6TmHceSqQMS0-kDuxkKe7IVqih4wZFy38C0L_yqio8kAurvS4cfO8mgMffyOqHlQzovzz0GN-uF8sYAQ6-fZQK82GpaUua6PX6BmUI9fJFFtNy3UOTUM37bN1MqGjsCGYAfaG4p0CrZEoihjPawWUZsJQacCfkFJrkzU-wMK4n1TcAMmL1Jt4t5TYxk-FOtzOWEESl9bjXfmL9n4pTrZ8Y5whxUPdkQniPMGLqOQLPjGJoL4f1V41jht6IRFzN9yvrOD88jSOS5JNwDBYVlc2-P6SFLMp57c3m_wVnXgbhFr_5Gp7B2rIR731daLS6l4XQ", "AQAB"));
    }

    private void storePublic(RSAPublicKeySpec publicKey) {
        ObjectOutputStream oout = null;
        try {
            oout = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream("/home/goblin/Desktop/key3.public")));
            BigInteger mod = publicKey.getModulus();
            BigInteger exp = publicKey.getPublicExponent();
            oout.writeObject(mod);
            oout.writeObject(exp);
        } catch (FileNotFoundException e) {
//			LOGGER.error("[storePublic] " + e.getMessage());
        } catch (IOException e) {
//			LOGGER.error("[storePublic] " + e.getMessage());
        } catch (Exception e) {
//			LOGGER.error("[storePublic]", e);
        } finally {
            try {
                if (oout != null) {
                    oout.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public String getErd() {
        return erd;
    }

    public void setErd(String erd) {
        this.erd = erd;
    }
}