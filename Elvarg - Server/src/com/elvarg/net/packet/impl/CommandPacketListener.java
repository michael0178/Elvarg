package com.elvarg.net.packet.impl;

import com.elvarg.cache.impl.definitions.ItemDefinition;
import com.elvarg.cache.impl.definitions.NpcDefinition;
import com.elvarg.cache.impl.definitions.ShopDefinition;
import com.elvarg.cache.impl.definitions.WeaponInterfaces;
import com.elvarg.engine.task.Task;
import com.elvarg.engine.task.TaskManager;
import com.elvarg.engine.task.impl.CombatPoisonEffect.PoisonType;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketListener;
import com.elvarg.world.World;
import com.elvarg.world.content.clan.ClanChatManager;
import com.elvarg.world.content.skills.SkillManager;
import com.elvarg.world.entity.combat.CombatFactory;
import com.elvarg.world.entity.combat.CombatSpecial;
import com.elvarg.world.entity.impl.npc.NPC;
import com.elvarg.world.entity.impl.object.GameObject;
import com.elvarg.world.entity.impl.player.Player;
import com.elvarg.world.model.Animation;
import com.elvarg.world.model.Flag;
import com.elvarg.world.model.Graphic;
import com.elvarg.world.model.Item;
import com.elvarg.world.model.PlayerRights;
import com.elvarg.world.model.Position;
import com.elvarg.world.model.Skill;

/**
 * This packet listener manages commands a player uses by using the command
 * console prompted by using the "`" char.
 * 
 * @author Gabriel Hannason
 */

public class CommandPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		String command = packet.readString();
		String[] parts = command.toLowerCase().split(" ");
		if (command.contains("\r") || command.contains("\n")) {
			return;
		}
		if (command.startsWith("/") && command.length() >= 1) {
			ClanChatManager.sendMessage(player, command.substring(1, command.length()));
			return;
		}
		try {
			switch (parts[0]){
			case "lockxp":
				player.setExperienceLocked(!player.experienceLocked());
				player.getPacketSender().sendMessage("Lock: " + player.experienceLocked());
				return;
			case "unlock":
				int type = Integer.parseInt(parts[1]);
				if (type == 0) {
					player.setPreserveUnlocked(true);
				} else if (type == 1) {
					player.setRigourUnlocked(true);
				} else if (type == 2) {
					player.setAuguryUnlocked(true);
				}
				player.getPacketSender().sendConfig(709, player.isPreserveUnlocked() ? 1 : 0);
				player.getPacketSender().sendConfig(711, player.isRigourUnlocked() ? 1 : 0);
				player.getPacketSender().sendConfig(713, player.isAuguryUnlocked() ? 1 : 0);
				return;
			case "bank":
			case "b":
				player.getBank(player.getCurrentBankTab()).open();
				return;
			case "tt":
					for (int i = 0; i < 100; i++) {
						World.getPlayers().add(player);
					}
				return;
			case "setlevel":
					Skill skill = Skill.values()[Integer.parseInt(parts[1])];
					int level = Integer.parseInt(parts[2]);
					player.getSkillManager().setCurrentLevel(skill, level).setMaxLevel(skill, level).setExperience(skill,
							SkillManager.getExperienceForLevel(level));
					WeaponInterfaces.assign(player);
				return;
			case "master":
					for (Skill skill1 : Skill.values()) {
						int level1 = SkillManager.getMaxAchievingLevel(skill1);
						player.getSkillManager().setCurrentLevel(skill1, level1).setMaxLevel(skill1, level1)
								.setExperience(skill1, SkillManager.getExperienceForLevel(level1));
					}
					WeaponInterfaces.assign(player);
				return;
			case "playnpc":
					player.setNpcTransformationId(Integer.parseInt(parts[1]));
					player.getUpdateFlag().flag(Flag.APPEARANCE);
				return;
			case "shopinv":
					int amt = 0;
					for (Item item : ShopDefinition.getShops().get(Integer.parseInt(parts[1])).getDefinition()
							.getOriginalStock()) {
						player.getInventory().add(item.getId(), item.getDefinition().isStackable() ? item.getAmount() : 1);
						amt++;
					}
					player.getPacketSender().sendMessage("Added " + amt + ", to your inventory.");
				return;
			case "npc":
					World.getNpcAddQueue().add(new NPC(Integer.parseInt(parts[1]), player.getPosition().copy().add(1, 0)));
				return;
			case "reloadnpcs":
					World.getNpcs().clear();
					TaskManager.submit(new Task(3) {
						@Override
						protected void execute() {
							NpcDefinition.init();
							stop();
						}
					});
				return;
			case "save":
					player.save();
				return;
			case "pos":
					player.getPacketSender().sendMessage(player.getPosition().toString());
				return;
			case "config":
					player.getPacketSender().sendConfig(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
				return;
			case "object":
					player.getPacketSender()
							.sendObject(new GameObject(Integer.parseInt(parts[1]), player.getPosition().copy()));
				return;
				
			case "spec":
					player.setSpecialPercentage(100);
					CombatSpecial.updateBar(player);
				return;
			case "runes":
					int[] runes = new int[] { 554, 555, 556, 557, 558, 559, 560, 561, 562, 563, 564, 565 };
					for (int rune : runes) {
						player.getInventory().add(rune, 1000);
					}
				return;
			case "tele":
					int x = Integer.parseInt(parts[1]);
					int y = Integer.parseInt(parts[2]);
					int z = 0;
					if (parts.length == 4) {
						z = Integer.parseInt(parts[3]);
					}
					player.moveTo(new Position(x, y, z));
				return;
			case "anim":
					int anim = Integer.parseInt(parts[1]);
					player.performAnimation(new Animation(anim));
				return;
			case "gfx":
					int gfx = Integer.parseInt(parts[1]);
					player.performGraphic(new Graphic(gfx));
				return;
			case "item":
					int amount = 1;
					if (parts.length > 2) {
						amount = Integer.parseInt(parts[2]);
					}
					player.getInventory().add(new Item(Integer.parseInt(parts[1]), amount));
				return;
			case "int":
					player.getPacketSender().sendInterface(Integer.parseInt(parts[1]));
				return;
			case "exp":
					Skill skill1 = Skill.forId(Integer.parseInt(parts[1]));
					player.getSkillManager().addExperience(skill1, 5000);
				return;
			case "fill":
					int bankIndex = 0;
					for (ItemDefinition def : ItemDefinition.getDefinitions()) {
						if (def == null) {
							continue;
						}
						if (def.isStackable() || def.isNoted()) {
							continue;
						}
						if (def.getValue() <= 0) {
							continue;
						}

						if (player.getBank(bankIndex).getFreeSlots() == 0) {
							bankIndex++;
						}

						if (bankIndex == 12) {
							break;
						}

						player.getBank(bankIndex).add(new Item(def.getId(), 1), false);
					}

				return;
			case "empty":
					player.getInventory().resetItems().refreshItems();
				return;
			case "poison":
					CombatFactory.poisonEntity(player, PoisonType.MILD);
				return;
			}
		
			/*
			 * switch (player.getRights()) {
			 * 
			 * }
			 */

			
		} catch (Exception exception) {
			exception.printStackTrace();

			if (player.getRights() == PlayerRights.DEVELOPER) {
				player.getPacketSender().sendMessage("Error executing that command.");

			} else {
				player.getPacketSender().sendMessage("Error executing that command.");
			}

		}
	}

	public static final int OP_CODE = 103;
}
