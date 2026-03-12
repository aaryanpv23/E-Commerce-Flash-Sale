import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FlashSaleInventoryManager {

    // productId -> stock
    private ConcurrentHashMap<String, AtomicInteger> inventory;

    // productId -> waiting list
    private ConcurrentHashMap<String, Queue<Integer>> waitingList;

    public FlashSaleInventoryManager() {
        inventory = new ConcurrentHashMap<>();
        waitingList = new ConcurrentHashMap<>();
    }

    // Add product with initial stock
    public void addProduct(String productId, int stock) {
        inventory.put(productId, new AtomicInteger(stock));
        waitingList.put(productId, new ConcurrentLinkedQueue<>());
    }

    // Check stock
    public int checkStock(String productId) {
        AtomicInteger stock = inventory.get(productId);
        if (stock == null) return 0;
        return stock.get();
    }

    // Purchase item
    public String purchaseItem(String productId, int userId) {

        AtomicInteger stock = inventory.get(productId);

        if (stock == null) {
            return "Product not found";
        }

        while (true) {
            int currentStock = stock.get();

            if (currentStock <= 0) {
                Queue<Integer> queue = waitingList.get(productId);
                queue.add(userId);
                return "Added to waiting list. Position: " + queue.size();
            }

            if (stock.compareAndSet(currentStock, currentStock - 1)) {
                return "Success! Remaining stock: " + (currentStock - 1);
            }
        }
    }

    // Get waiting list
    public Queue<Integer> getWaitingList(String productId) {
        return waitingList.get(productId);
    }

    // Demo
    public static void main(String[] args) {

        FlashSaleInventoryManager manager = new FlashSaleInventoryManager();

        manager.addProduct("IPHONE15_256GB", 3);

        System.out.println("Stock: " + manager.checkStock("IPHONE15_256GB"));

        System.out.println(manager.purchaseItem("IPHONE15_256GB", 12345));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 67890));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 22222));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 99999));

        System.out.println("Waiting List: " + manager.getWaitingList("IPHONE15_256GB"));
    }
}