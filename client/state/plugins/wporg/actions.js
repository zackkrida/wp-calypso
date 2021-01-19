/**
 * Internal dependencies
 */
import { fetchPluginInformation } from 'calypso/lib/wporg';
import { isFetching } from 'calypso/state/plugins/wporg/selectors';
import { normalizePluginData } from 'calypso/lib/plugins/utils';
import {
	PLUGINS_WPORG_PLUGIN_RECEIVE,
	PLUGINS_WPORG_PLUGIN_REQUEST,
} from 'calypso/state/action-types';

import 'calypso/state/plugins/init';

export function fetchPluginData( pluginSlug ) {
	return async ( dispatch, getState ) => {
		if ( isFetching( getState(), pluginSlug ) ) {
			return;
		}

		dispatch( {
			type: PLUGINS_WPORG_PLUGIN_REQUEST,
			pluginSlug,
		} );

		try {
			const data = await fetchPluginInformation( pluginSlug );

			dispatch( {
				type: PLUGINS_WPORG_PLUGIN_RECEIVE,
				pluginSlug,
				data: normalizePluginData( { detailsFetched: Date.now() }, data ),
			} );
		} catch ( error ) {
			dispatch( {
				type: PLUGINS_WPORG_PLUGIN_RECEIVE,
				pluginSlug,
				error,
			} );
		}
	};
}
