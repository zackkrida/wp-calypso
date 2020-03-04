/**
 * External dependencies
 */
import React from 'react';
import { useSelector } from 'react-redux';
import { useTranslate } from 'i18n-calypso';
/**
 * Internal dependencies
 */
import Gravatar from 'components/gravatar';
import JetpackLogo from 'components/jetpack-logo';
import SocialLogo from 'components/social-logo';
import { getUser } from 'state/users/selectors';

interface Props {
	avatarUrl: string;
	name: string;
	role: string;
	type: 'Application' | 'Happiness Engineer' | 'Person';
	wpcomId: number;
}

interface User {
	display_name: string;
}

const AVATAR_SIZE = 25;

const renderAvatar = (
	avatarUrl: string,
	name: string,
	type: 'Application' | 'Happiness Engineer' | 'Person'
) => {
	switch ( type ) {
		case 'Application':
			switch ( name ) {
				case 'WordPress':
					return <SocialLogo icon="wordpress" size={ AVATAR_SIZE } />;
				case 'Jetpack':
					return <JetpackLogo size={ AVATAR_SIZE } />;
			}
		case 'Happiness Engineer':
			return <JetpackLogo size={ AVATAR_SIZE } />;
		case 'Person':
			return <Gravatar user={ { avatar_URL: avatarUrl } } size={ AVATAR_SIZE } />;
	}
};

const LogItemActor = ( { avatarUrl, name, role, type, wpcomId }: Props ) => {
	const user: User | null = useSelector( state => getUser( state, wpcomId ) );
	const translate = useTranslate();

	const renderRole = ( roleArg: string ) => {
		switch ( roleArg ) {
			case 'administrator':
				return translate( 'Administrator' );
			case 'super admin':
				return translate( 'Super Admin' );
			case 'editor':
				return translate( 'Editor' );
			case 'author':
				return translate( 'Author' );
			case 'contributor':
				return translate( 'Contributor' );
			case 'subscriber':
				return translate( 'Subscriber' );
			case 'follower':
				return translate( 'Follower' );
			default:
				return roleArg;
		}
	};

	const displayName = user && user.display_name ? user.display_name : name;

	return (
		<div className="log-item__actor">
			{ renderAvatar( avatarUrl, name, type ) }
			<p className="log-item__actor-description">
				{ displayName }
				{ type === 'Person' ? `, ${ renderRole( role ) }` : '' }
			</p>
		</div>
	);
};

export default LogItemActor;
