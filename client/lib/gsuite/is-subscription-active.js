/**
 * External dependencies
 */
import { get } from 'lodash';

export function isSubscriptionActive( domain ) {
	const status = get( domain, 'googleAppsSubscription.status', '' );

	return 'active' === status;
}
