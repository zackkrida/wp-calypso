/**
 * Internal dependencies
 */
import 'calypso/state/plugins/init';

export function getAllPlugins( state ) {
	return state?.plugins.wporg.items;
}

export function getPlugin( state, pluginSlug ) {
	const plugin = state?.plugins.wporg.items[ pluginSlug ] ?? null;
	return plugin ? { ...plugin } : plugin;
}

export function isFetching( state, pluginSlug ) {
	return state?.plugins.wporg.fetchingItems[ pluginSlug ] ?? false;
}

export function isFetched( state, pluginSlug ) {
	const plugin = getPlugin( state, pluginSlug );
	return plugin ? !! plugin.fetched : false;
}
