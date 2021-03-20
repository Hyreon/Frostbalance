package botmanager.frostbalance;

import botmanager.frostbalance.command.ArgumentStream;
import botmanager.frostbalance.command.MessageContext;
import botmanager.frostbalance.menu.Menu;
import botmanager.frostbalance.menu.response.MenuResponse;
import botmanager.frostbalance.menu.response.SimpleTextHook;
import botmanager.frostbalance.resource.Inventory;
import botmanager.frostbalance.resource.ItemStack;
import botmanager.frostbalance.resource.ItemType;
import botmanager.frostbalance.resource.QualityGrade;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.NoSuchElementException;

public class TradeMenu extends Menu {

    boolean success = false;
    FailureReason failureReason = FailureReason.REFUSED;

    Inventory itemsToGive = new Inventory(true);
    Inventory itemsToTake = new Inventory(true);
    boolean approved;

    UserWrapper otherMerchant;
    boolean swapped;

    public final MenuResponse PERFORM_TRADE = new MenuResponse("✅", "Perform trade") {

        @Override
        public void reactEvent() {
            if (approved) {
                trade();
                close(false);
            } else {
                swapUsers(true);
                updateMessage();
            }
        }

        @Override
        public boolean isValid() {
            return true;
        }
    };

    private void trade() {
        if (swapped) swapUsers(true);
        if (getActor() == null) {
            failureReason = FailureReason.ERROR;
            return;
        } else {
            for (ItemStack item : itemsToGive.getItems()) {
                if (!getActor().playerIn(getContext().getGameNetwork()).getCharacter().getInventory().hasItem(item)) {
                    failureReason = FailureReason.SHORTAGE;
                    return;
                }
            }
            for (ItemStack item : itemsToTake.getItems()) {
                if (otherMerchant.playerIn(getContext().getGameNetwork()).getCharacter().getInventory().hasItem(item)) {
                    failureReason = FailureReason.SHORTAGE;
                    return;
                }
            }
            try {
                for (ItemStack item : itemsToGive.getItems()) {
                    getActor().playerIn(getContext().getGameNetwork()).getCharacter().getInventory().removeItem(item);
                    otherMerchant.playerIn(getContext().getGameNetwork()).getCharacter().getInventory().addItem(item);
                }
                for (ItemStack item : itemsToTake.getItems()) {
                    otherMerchant.playerIn(getContext().getGameNetwork()).getCharacter().getInventory().removeItem(item);
                    getActor().playerIn(getContext().getGameNetwork()).getCharacter().getInventory().addItem(item);
                }
                success = true;
            } catch (IllegalArgumentException e) {
                failureReason = FailureReason.ERROR;
            }

        }

    }

    private boolean inRange() {
        GameNetwork network = getContext().getGameNetwork();
        return getActor() != null &&
                otherMerchant.playerIn(network).getCharacter().getLocation().minimumDistance(getActor().playerIn(network).getCharacter().getLocation()) <= 1;
    }

    public final MenuResponse SWAP_TRADE = new MenuResponse("\uD83D\uDD00", "Swap trader") {

        @Override
        public void reactEvent() {
            swapUsers(false);
            updateMessage();
        }

        @Override
        public boolean isValid() {
            return true;
        }
    };

    private void swapUsers(boolean approved) {
        UserWrapper middle = otherMerchant;
        otherMerchant = getActor();
        setActor(middle);
        this.swapped = !swapped;
        this.approved = approved;
    }

    public final MenuResponse REFUSE_TRADE = new MenuResponse("❎", "Refuse trade") {

        @Override
        public void reactEvent() {
            failureReason = FailureReason.REFUSED;
            close(false);
        }

        @Override
        public boolean isValid() {
            return true;
        }
    };

    public final MenuResponse EXPIRE_CHECK = new MenuResponse("❓", "Expire trade") {

        @Override
        public void reactEvent() {
            failureReason = FailureReason.EXPIRED;
            close(false);
        }

        @Override
        public boolean isValid() {
            return false;
        }

    };

    public TradeMenu(Frostbalance bot, MessageContext context, UserWrapper otherMerchant) {
        super(bot, context);
        this.otherMerchant = otherMerchant;

        menuResponses.add(PERFORM_TRADE);
        menuResponses.add(SWAP_TRADE);
        menuResponses.add(REFUSE_TRADE);

        //TODO ensure no resource ever begins with the name of a material grade
        hook(new SimpleTextHook(this, "Type <QUANTITY> [GRADE] <RESOURCE>; eg -2 Crude Iron Ore") {

            @Override
            public void hookEvent(@NotNull MessageContext hookContext) {
                ArgumentStream stream = new ArgumentStream(hookContext.getMessage().getContentRaw().split(" "));
                boolean otherMerchant = hookContext.getAuthor() == getOtherMerchant();
                Integer change = stream.nextInteger();
                System.out.println("Change:" + change);
                if (change == null) return;
                String all = stream.exhaust(0);
                String itemId = all;
                QualityGrade minGrade = QualityGrade.CRUDE;
                for (QualityGrade grade : QualityGrade.values()) {
                    if (all.startsWith(grade.displayName())) {
                        itemId = all.replaceFirst(grade.displayName() + " ", "");
                        minGrade = grade;
                        break;
                    }
                }
                ItemType type;
                try {
                    type = Frostbalance.bot.itemWithId(itemId);
                } catch (NoSuchElementException e) {
                    return;
                }
                ItemStack itemStack = new ItemStack(type, Math.abs(change), minGrade.ordinal(), true);
                System.out.println("ItemStack:" + itemStack);
                if (change < 0) {
                    if (otherMerchant ^ swapped) {
                        itemsToTake.removeItem(itemStack);
                    } else {
                        itemsToGive.removeItem(itemStack);
                    }
                } else {
                    if (otherMerchant ^ swapped) {
                        itemsToTake.addItem(itemStack);
                    } else {
                        itemsToGive.addItem(itemStack);
                    }
                }

                approved = false;
                updateMessage();
            }

            @Override
            public boolean isValid(@NotNull MessageContext hookContext) {
                return (hookContext.getAuthor() == getOtherMerchant() || hookContext.getAuthor() == getActor())
                        && getMenu().getOriginalMenu().getMessage() != null
                        && hookContext.getChannel() == getMenu().getOriginalMenu().getMessage().getChannel();
            }
        });

    }

    @Override
    public EmbedBuilder getEmbedBuilder() {
        EmbedBuilder builder = new EmbedBuilder();
        String otherMerchantName = getContext().getGuild().getMember(otherMerchant).getEffectiveName();
        String currentMerchantName = getActor().memberIn(getContext().getGuild().getId()).getEffectiveName();
        if (isClosed()) {
            if (success) {
                builder.setColor(getBot().getGuildWrapper(getContext().getGuild().getId()).getColor());
                builder.setTitle(currentMerchantName + " has successfully traded with " + otherMerchantName + ".");
                builder.setDescription(tradeYields());
            } else {
                builder.setColor(Color.DARK_GRAY);
                builder.setTitle(currentMerchantName + ": Trade failed!");
                if (failureReason == FailureReason.REFUSED)
                    builder.setDescription("You stopped negotiating.");
                else if (failureReason == FailureReason.EXPIRED)
                    builder.setDescription("The trade request expired, and a new one was placed in the same channel.");
                else if (failureReason == FailureReason.SHORTAGE)
                    builder.setDescription("Someone didn't turn up the goods.");
                else if (failureReason == FailureReason.ERROR)
                    builder.setDescription("There was an error with the request.");
            }
        } else {
            builder.setColor(getBot().getGuildWrapper(getContext().getGuild().getId()).getColor());
            builder.setTitle(currentMerchantName + " is trading with " + otherMerchantName + ".");
            builder.setDescription(tradeYields());
        }
        return builder;
    }

    private String tradeYields() {
        return String.join("\n",
                "**" + getActor().memberIn(getContext().getGuild()).getEffectiveName() + " gives:**",
                !swapped ? itemsToGive.render() : itemsToTake.render(),
                "**" + getContext().getGuild().getMember(otherMerchant).getEffectiveName() + " gives:**",
                !swapped ? itemsToTake.render() : itemsToGive.render());
    }

    public UserWrapper getOtherMerchant() {
        return otherMerchant;
    }

    private enum FailureReason {
        REFUSED, EXPIRED, SHORTAGE, ERROR
    }
}
