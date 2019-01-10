package net.zerofill.repositories;

import net.zerofill.domains.Base;
import net.zerofill.domains.Weapon;
import net.zerofill.domains.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.util.List;

public class GameRepository extends BaseRepository {
    private static final Logger logger = LoggerFactory.getLogger(GameRepository.class);

    public static Long createRecord(Base record) {
        try (Session sessionObj = getSessionFactory().openSession()) {
            Transaction transObj = sessionObj.beginTransaction();
            sessionObj.save(record);
            transObj.commit();
            return record.getId();
        }
    }

    @SuppressWarnings("unchecked")
    public static List getUsers() {
        try (Session sessionObj = getSessionFactory().openSession()) {
            List studentsList = sessionObj.createQuery("FROM User").list();
            return studentsList;
        }
    }

    public static Base updateRecord(Base updateObj) {
        try (Session sessionObj = getSessionFactory().openSession()) {
            Transaction transObj = sessionObj.beginTransaction();
            sessionObj.update(updateObj);
            transObj.commit();
            return updateObj;
        } catch (Exception e) {
            logger.debug(e.getLocalizedMessage());
        }

        return null;
    }

    public static void deleteUser(Long userId) {
        try (Session sessionObj = getSessionFactory().openSession()) {
            Transaction transObj = sessionObj.beginTransaction();
            User userObj = findUserById(userId);
            sessionObj.delete(userObj);
            transObj.commit();
        }
    }

    public static User findUserById(Long userId) {
        try (Session sessionObj = getSessionFactory().openSession()) {
            return sessionObj.load(User.class, userId);
        }
    }

    @SuppressWarnings("unchecked")
    public static User findUserByDiscordUser(String discordUser) {
        try (Session sessionObj = getSessionFactory().openSession()) {
            return (User) sessionObj.createQuery("FROM User WHERE discordUser = :discordUser").setParameter("discordUser", discordUser).getSingleResult();
        } catch (NoResultException nre) {
            if (logger.isDebugEnabled()) {
                logger.debug("No results found.");
            }
        }
        return null;
    }

    public static void deleteAllUsers() {
        try (Session sessionObj = getSessionFactory().openSession()) {
            Transaction transObj = sessionObj.beginTransaction();
            Query queryObj = sessionObj.createQuery("DELETE FROM users");
            queryObj.executeUpdate();
            transObj.commit();
        }
    }

    public static Weapon findWeaponRandomly() {
        try (Session sessionObj = getSessionFactory().openSession()) {
            NativeQuery query = sessionObj.createSQLQuery("SELECT id, name_japanese AS name, `atkmatk` AS atk, weapon_level AS level, 0 AS refine, 0 AS user_id FROM item_db_re WHERE type = 5 ORDER BY RAND() ASC LIMIT 1");
            query.addEntity(Weapon.class);
            return (Weapon) query.getSingleResult();
        }
    }

    public static Weapon findWeaponById(Long weaponId) {
        try (Session sessionObj = getSessionFactory().openSession()) {
            return (Weapon) sessionObj.createQuery("FROM Weapon WHERE id = :id").setParameter("id", weaponId).getSingleResult();
        } catch (NoResultException nre) {
            if (logger.isDebugEnabled()) {
                logger.debug("No results found.");
            }
        }
        return null;
    }

    public static void deleteWeapon(Long weaponId) {
        try (Session sessionObj = getSessionFactory().openSession()) {
            Transaction transObj = sessionObj.beginTransaction();
            Weapon weaponObj = findWeaponById(weaponId);
            sessionObj.delete(weaponObj);
            transObj.commit();
        }
    }

    public static void deleteAllWeapons() {
        try (Session sessionObj = getSessionFactory().openSession()) {
            Transaction transObj = sessionObj.beginTransaction();
            Query queryObj = sessionObj.createQuery("DELETE FROM weapons");
            queryObj.executeUpdate();
            transObj.commit();
        }
    }
}
