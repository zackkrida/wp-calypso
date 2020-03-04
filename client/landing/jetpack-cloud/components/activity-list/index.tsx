/**
 * External dependencies
 */
import React from 'react';

/**
 * Internal dependencies
 */
import { LogData } from './types';
import LogItem from '../log-item';
// import FormattedBlock from 'components/notes-formatted-block';

interface Props {
	logs?: LogData;
}

class ActivityList extends React.PureComponent< Props > {
	render() {
		if ( ! this.props.logs ) {
			return [];
		}

		const {
			logs: { data: logItems, state },
		} = this.props;

		if ( 'success' !== state || ! Array.isArray( logItems ) ) {
			return false;
		}

		if ( 0 === logItems.length ) {
			return <p className="activity-list__no-items">No backups found.</p>;
		}

		return logItems.map( log => <LogItem key={ log.activityId } log={ log } /> );
	}
}

export default ActivityList;
