/**
 * External dependencies
 */
import React from 'react';

/**
 * Internal dependencies
 */
import { LogItemType } from '../activity-list/types';
import { Card } from '@automattic/components';
import CardHeading from 'components/card-heading';
import LogItemActor from './actor';

import './style.scss';

export interface Props {
	log: LogItemType;
}

const LogItem = ( {
	log: { actorAvatarUrl, actorName, actorRole, activityTitle, actorType, actorWpcomId },
}: Props ) => (
	<div>
		<Card>
			<LogItemActor
				avatarUrl={ actorAvatarUrl }
				name={ actorName }
				role={ actorRole }
				type={ actorType }
				wpcomId={ actorWpcomId }
			/>
			<CardHeading>{ activityTitle }</CardHeading>
		</Card>
	</div>
);

export default LogItem;
