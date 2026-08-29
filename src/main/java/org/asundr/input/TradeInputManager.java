package org.asundr.input;

import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

// Registers and dispatches events for key presses and releases
public class TradeInputManager implements KeyListener
{
	private static final TradeInputManager instance = new TradeInputManager();
	private static final HashMap<Integer, ArrayList<Consumer<Boolean>>> keyPressedListeners = new HashMap<>();
	private static final HashMap<Integer, ArrayList<Consumer<Boolean>>> keyReleasedListeners = new HashMap<>();

	public static void initialize(KeyManager keyManager)
	{
		keyManager.registerKeyListener(instance);
	}

	public static void shutdown(KeyManager keyManager)
	{
		keyManager.unregisterKeyListener(instance);
	}

	/// //

	public static void registerReleasedListener(final int virtualKey, final Consumer<Boolean> callback)
	{
		registerListener(keyReleasedListeners, virtualKey, callback);
	}

	public static void unregisterReleasedListener(final int virtualKey, final Consumer<Boolean> callback)
	{
		unregisterListener(keyReleasedListeners, virtualKey, callback);
	}

	public static void registerPressedListener(final int virtualKey, final Consumer<Boolean> callback)
	{
		registerListener(keyPressedListeners, virtualKey, callback);
	}

	public static void unregisterPressedListener(final int virtualKey, final Consumer<Boolean> callback)
	{
		unregisterListener(keyPressedListeners, virtualKey, callback);
	}

	private static void registerListener(final HashMap<Integer, ArrayList<Consumer<Boolean>>> map, final int keyCode, final Consumer<Boolean> callback)
	{
		if (!map.containsKey(keyCode))
		{
			map.putIfAbsent(keyCode, new ArrayList<>());
		}
		map.get(keyCode).add(callback);
	}

	private static void unregisterListener(final HashMap<Integer, ArrayList<Consumer<Boolean>>> map, final int virtualKey, final Consumer<Boolean> callback)
	{
		if (!map.containsKey(virtualKey))
		{
			return;
		}
		map.get(virtualKey).remove(callback);
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (keyPressedListeners.containsKey(e.getKeyCode()))
		{
			keyPressedListeners.get(e.getKeyCode()).forEach(l -> l.accept(true));
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		if (keyPressedListeners.containsKey(e.getKeyCode()))
		{
			keyPressedListeners.get(e.getKeyCode()).forEach(l -> l.accept(false));
		}
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
	}
}
