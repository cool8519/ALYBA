package dal.util.db;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

public class ObjectDBUtil {

	private static final String DEFAULT_DBFILE_PATH = "dbfile.odb";
	private static ObjectDBUtil instance = null;
	
	private EntityManagerFactory emf = null;
	
	
	public static ObjectDBUtil getInstance() {
		if(instance == null) {
            synchronized(ObjectDBUtil.class) {
                if(instance == null) {
					instance = new ObjectDBUtil(DEFAULT_DBFILE_PATH);
                }
            }
		}
		return instance;
	}
	
	public static void register(ObjectDBUtil objectdb) {
		instance = objectdb;
	}

	public static boolean isRegistered() {
		return instance != null;
	}

	public ObjectDBUtil(String path) {
		this.emf = Persistence.createEntityManagerFactory("objectdb:" + path);
	}

	public ObjectDBUtil(String path, boolean drop) {
		this.emf = Persistence.createEntityManagerFactory("objectdb:" + path + (drop?";drop":""));
	}

	public EntityManager createEntityManager() {
		return emf.createEntityManager();
	}
	
	public boolean isReady(EntityManager em) {
		return (em != null && em.isOpen());
	}
	
	public void checkReady(EntityManager em) throws Exception {
		if(em == null || !em.isOpen()) {
			throw new Exception("Database has been closed or wasn't initialized");
		}
	}
	
	public String getDBFilePath() {
		Map<String,Object> prop = emf.getProperties();
		String filepath = prop.get("objectdb.connection.path").toString();
		if(filepath.startsWith("/") || filepath.matches("^[A-Z]:\\\\.*")) {
			return filepath;
		} else {
			return prop.get("objectdb.home").toString() + File.separatorChar + filepath;
		}
	}
	
	public void setAutoFlush(EntityManager em, boolean flag) throws Exception {
		checkReady(em);
		em.setFlushMode(flag ? FlushModeType.AUTO : FlushModeType.COMMIT);
	}
	
	public void beginTransaction(EntityManager em) throws Exception {
		checkReady(em);
		em.getTransaction().begin();
	}

	public void commitTransaction(EntityManager em) throws Exception {
		commitTransaction(em, true, true);
	}

	public void commitTransaction(EntityManager em, boolean flush, boolean clear) throws Exception {
		checkReady(em);
		em.getTransaction().commit();
		if(flush) {
			flush(em, clear);
		}
	}

	public void flush(EntityManager em, boolean clear) throws Exception {
		checkReady(em);
		em.flush();
		if(clear) {
			em.clear();
		}
	}
	
	public void clear(EntityManager em) throws Exception {
		checkReady(em);
		em.clear();
	}
	
	public void rollbackTransaction(EntityManager em, boolean flush) throws Exception {
		checkReady(em);
		em.getTransaction().rollback();
		if(flush) {
			flush(em, true);
		}
	}
	
	public void close(EntityManager em) {
		try {
			flush(em, true);
		} catch(Exception e) {			
		}
		if(em != null) {
			try {
				em.close();
			} catch(Exception e) {
			} finally {
				em = null;
			}
		}
	}

	public void closeAll() {
		if(emf != null) {
			try {
				emf.close();
			} catch(Exception e) {
			}
		}
		if(this == emf) {
			emf = null;
		}		
		if(this == instance) {
			instance = null;
		}
	}

	public void closeAndDeleteDB() {
		if(emf != null) {
			try {
				emf.close();
			} catch(Exception e) {
			}			
			try {
				new File(getDBFilePath()).delete();
			} catch(Exception e) {
			}
		}
		if(this == emf) {
			emf = null;
		}
		if(this == instance) {
			instance = null;
		}
	}

	public <E> long count(EntityManager em, Class<E> clazz) throws Exception {
		return select(em, "SELECT COUNT(o) FROM " + clazz.getName() + " AS o", Long.class, null);
	}

	public <E> long count(EntityManager em, String sql) throws Exception {
		return select(em, sql, Long.class, null);
	}

	public <E> E select(EntityManager em, Class<E> clazz) throws Exception {
		return select(em, "SELECT o FROM " + clazz.getName() + " AS o", clazz, null);
	}
	
	public <E> E select(EntityManager em, String sql, Class<E> clazz, Map<String,Object> params) throws Exception {
		checkReady(em);
		try {
			TypedQuery<E> query = (TypedQuery<E>)em.createQuery(sql, clazz);
			if(params != null) {
				for(String name : params.keySet()) {
					query.setParameter(name, params.get(name));
				}
			}
			E result = query.getSingleResult();
			return result;		
		} catch(NoResultException nre) {
			return null;
		} catch(Exception e) {
			throw e;
		}
	}

	public <E> List<E> selectList(EntityManager em, Class<E> clazz) throws Exception {
		return selectList(em, "SELECT o FROM " + clazz.getName() + " AS o", clazz, null);
	}

	public <E> List<E> selectList(EntityManager em, String sql, Class<E> clazz, Map<String,Object> params) throws Exception {
		checkReady(em);
		try {
			TypedQuery<E> query = (TypedQuery<E>)em.createQuery(sql, clazz);
			if(params != null) {
				for(String name : params.keySet()) {
					query.setParameter(name, params.get(name));
				}
			}
			List<E> results = query.getResultList();
			return results;		
		} catch(Exception e) {
			throw e;
		}
	}

	public void insert(EntityManager em, Object obj) throws Exception {
		checkReady(em);
		em.persist(obj);
	}

	public void insertWithTransaction(EntityManager em, Object obj, boolean flush) throws Exception {
		try {
			beginTransaction(em);
			insert(em, obj);
			commitTransaction(em, flush, false);
		} catch(Exception e) {
			rollbackTransaction(em, flush);
			throw e;
		} 
	}

	public void delete(EntityManager em, Object obj) throws Exception {
		checkReady(em);
		em.remove(obj);
	}

	public <E> void delete(EntityManager em, Object key, Class<E> clazz) throws Exception {
		checkReady(em);
		E obj = em.find(clazz, key);
		em.remove(obj);
	}

	public <E> void deleteWithTransaction(EntityManager em, Object key, Class<E> clazz, boolean flush) throws Exception {
		try {
			beginTransaction(em);
			delete(em, key, clazz);
			commitTransaction(em, flush, false);
		} catch(Exception e) {
			rollbackTransaction(em, flush);
			throw e;
		} 		
	}
	
	public int delete(EntityManager em, String sql, Map<String,Object> params) throws Exception {
		checkReady(em);
		try {
			Query query = em.createQuery(sql);
			if(params != null) {
				for(String name : params.keySet()) {
					query.setParameter(name, params.get(name));
				}
			}
			int result = query.executeUpdate();
			return result;		
		} catch(Exception e) {
			throw e;
		}
	}

	public int deleteWithTransaction(EntityManager em, String sql, Map<String,Object> params, boolean flush) throws Exception {
		try {
			beginTransaction(em);
			int result = delete(em, sql, params);
			commitTransaction(em, flush, false);
			return result;
		} catch(Exception e) {
			rollbackTransaction(em, flush);
			throw e;
		} 		
	}

}
