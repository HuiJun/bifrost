package net.zerofill.game;

import net.zerofill.domains.UserMetadata;
import net.zerofill.domains.Weapon;
import net.zerofill.domains.User;
import net.zerofill.repositories.GameRepository;
import net.zerofill.utils.BotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class RefineGame {
    private static final Logger logger = LoggerFactory.getLogger(RefineGame.class);

    private static final Map<Integer, List<Integer>> REFINE_RATES = new HashMap<>();

    static {
        REFINE_RATES.put(1, Arrays.asList(100, 100, 100, 100, 100, 100, 100, 100, 60, 40, 19, 18, 18, 18, 18, 17, 17, 17, 17, 15, 15));
        REFINE_RATES.put(2, Arrays.asList(100, 100, 100, 100, 100, 100, 100, 60, 40, 20, 19, 18, 18, 18, 18, 17, 17, 17, 17, 15, 15));
        REFINE_RATES.put(3, Arrays.asList(100, 100, 100, 100, 100, 100, 60, 50, 20, 20, 19, 18, 18, 18, 18, 17, 17, 17, 17, 15, 15));
        REFINE_RATES.put(4, Arrays.asList(100, 100, 100, 100, 100, 60, 40, 40, 20, 20, 9, 8, 8, 8, 8, 7, 7, 7, 7, 5, 5));
    }

    public static List<String> refineWeapon(UserMetadata userName, boolean safe) {
        List<String> response = new ArrayList<>();
        User user = GameRepository.findUserByDiscordUser(userName.getDiscordName());
        if (user == null) {
            user = new User();
            user.setDiscordUser(userName.getDiscordName());
            user.setHealth(100);
            GameRepository.createRecord(user);
        }

        Weapon weapon = GameRepository.findWeaponById(user.getId());
        if (weapon != null) {
            Map.Entry<String, Weapon> newWeaponMap = refine(weapon, safe).entrySet().iterator().next();
            Weapon newWeapon = newWeaponMap.getValue();
            GameRepository.updateRecord(newWeapon);
            response.add(newWeaponMap.getKey());
        } else {
            response.add(assignWeapon(user, userName));
        }

        return response;
    }

    public static List<String> showWeapon(UserMetadata userName) {
        List<String> response = new ArrayList<>();
        User user = GameRepository.findUserByDiscordUser(userName.getDiscordName());
        if (user == null) {
            response.add(String.format("Pimpin' ain't easy. Try %s%s", BotUtils.BOT_PREFIX, "refine"));
            return response;
        }
        Weapon weapon = GameRepository.findWeaponById(user.getId());
        if (weapon == null) {
            response.add(String.format("%s shows off their +3 birthday suit.", user.getDiscordUser()));
            return response;
        }
        response.add(String.format("%s shows off a +%d %s (Level %d)", user.getDiscordUser(), weapon.getRefine(), weapon.getName(), weapon.getLevel()));
        return response;
    }

    public static boolean checkUser() {
        return true;
    }

    public static Map<String, Weapon> refine(Weapon weapon) {
        return refine(weapon, false);
    }

    public static String assignWeapon(User user, UserMetadata userName) {
        Weapon weapon = GameRepository.findWeaponRandomly();
        weapon.setId(user.getId());
        GameRepository.createRecord(weapon);
        return String.format("RNGesus gives %s %s (Level %d) to %s", "aeiou".indexOf(weapon.getName().toLowerCase().charAt(0)) >= 0 ? "an" : "a", weapon.getName(), weapon.getLevel(), userName.getDisplayName());
    }

    public static Map<String, Weapon> refine(Weapon weapon, boolean safe) {
        Map<String, Weapon> response = new HashMap<>();

        int level = weapon.getLevel();
        int refine = weapon.getRefine();

        List<Integer> refineList = REFINE_RATES.getOrDefault(level, new ArrayList<>());

        if (refine + 1 >= refineList.size()) {
            response.put("Can't refine this weapon", weapon);
            return response;
        }

        int success = refineList.get(refine + 1);
        int chance = ThreadLocalRandom.current().nextInt(0, 100);

        StringBuilder status = new StringBuilder();

        if (chance >= (100 - success)) {
            if (safe) {
                success = refineList.get(refine + 1);
                while (100 - success == 0) {
                    refine++;
                    success = refineList.get(refine + 1);
                }
            } else {
                refine++;
            }
            weapon.setRefine(refine);
            status.append(String.format("Put on +%d %s (Level %d)", refine, weapon.getName(), weapon.getLevel()));
        } else {
            if (refine < 10) {
                GameRepository.deleteWeapon(weapon.getId());
                status.append(String.format("+%d %s has broken.", refine, weapon.getName()));
                weapon = null;
            } else {
                int newChance = ThreadLocalRandom.current().nextInt(0, 100);
                if (newChance > 95) {
                    GameRepository.deleteWeapon(weapon.getId());
                    status.append(String.format("+%d %s has broken.", refine, weapon.getName()));
                    weapon = null;
                } else {
                    refine = refine - 3;
                    weapon.setRefine(refine);
                    status.append(String.format("Put on +%d %s", refine, weapon.getName()));
                }
            }
        }

        response.put(status.toString(), weapon);

        return response;
    }


}
