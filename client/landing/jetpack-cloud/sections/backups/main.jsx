/**
 * External dependencies
 */
import React, { Component } from 'react';
import { connect } from 'react-redux';

/**
 * Internal dependencies
 */
import { getActivityLogFilter } from 'state/selectors/get-activity-log-filter';
import { getSelectedSiteId } from 'state/ui/selectors';
import { requestActivityLogs } from 'state/data-getters';
import ActivityList from '../../components/activity-list';

class BackupsPage extends Component {
	render() {
		return (
			<div>
				<p>Welcome to the backup detail page for site { this.props.siteId }</p>
				<ActivityList logs={ this.props.logs } />
			</div>
		);
	}
}

export default connect( state => {
	const siteId = getSelectedSiteId( state );
	const filter = getActivityLogFilter( state, siteId );
	const logs = requestActivityLogs( siteId, filter );

	return {
		siteId,
		logs,
	};
} )( BackupsPage );
