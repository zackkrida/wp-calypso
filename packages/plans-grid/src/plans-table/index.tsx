/**
 * External dependencies
 */
import React, { useState } from 'react';
import { useSelect } from '@wordpress/data';
import type { DomainSuggestions } from '@automattic/data-stores';

/**
 * Internal dependencies
 */
import PlanItem from './plan-item';
import { PLANS_STORE } from '../constants';
import type { BillingIntervalType } from '../plans-interval-toggle';
import type { CTAVariation, PopularBadgeVariation, CustomTagLinesMap } from './types';

/**
 * Style dependencies
 */
import './style.scss';

export interface Props {
	selectedPlanSlug: string;
	onPlanSelect: ( planSlug: string ) => void;
	onPickDomainClick?: () => void;
	currentDomain?: DomainSuggestions.DomainSuggestion;
	disabledPlans?: { [ planSlug: string ]: string };
	locale: string;
	showTaglines?: boolean;
	CTAVariation?: CTAVariation;
	popularBadgeVariation: PopularBadgeVariation;
	customTagLines?: CustomTagLinesMap;
	defaultAllPlansExpanded?: boolean;
	billingInterval: BillingIntervalType;
	onMaxMonhtlyDiscountPercentageChange: ( perc: number | undefined ) => void;
}

const PlansTable: React.FunctionComponent< Props > = ( {
	selectedPlanSlug,
	onPlanSelect,
	onPickDomainClick,
	currentDomain,
	disabledPlans,
	locale,
	billingInterval,
	onMaxMonhtlyDiscountPercentageChange,
	showTaglines = false,
	CTAVariation = 'NORMAL',
	popularBadgeVariation = 'ON_TOP',
	customTagLines,
	defaultAllPlansExpanded = false,
} ) => {
	// TODO: use billingInterval to query the correct plans (ANNUALLY or MONTHLY)
	const supportedPlans = useSelect( ( select ) => select( PLANS_STORE ).getSupportedPlans() );
	const prices = useSelect( ( select ) => select( PLANS_STORE ).getPrices( locale ) );
	const [ allPlansExpanded, setAllPlansExpanded ] = useState( defaultAllPlansExpanded );

	// TODO: replace tempDiscountPlaceholder with a call to the new data-store selector
	// to get the annually vs monthly discount for each plan
	// TODO: when discounts data updates, call onMaxMonhtlyDiscountPercentageChange prop
	const tempDiscountPlaceholder = 43;
	React.useEffect( () => {
		onMaxMonhtlyDiscountPercentageChange( tempDiscountPlaceholder );
	}, [ onMaxMonhtlyDiscountPercentageChange, tempDiscountPlaceholder ] );

	return (
		<div className="plans-table">
			{ supportedPlans.map(
				( plan ) =>
					plan && (
						<PlanItem
							popularBadgeVariation={ popularBadgeVariation }
							allPlansExpanded={ allPlansExpanded }
							key={ plan.storeSlug }
							slug={ plan.storeSlug }
							domain={ currentDomain }
							tagline={ ( showTaglines && customTagLines?.[ plan.storeSlug ] ) ?? plan.description }
							CTAVariation={ CTAVariation }
							features={ plan.features ?? [] }
							billingInterval={ billingInterval }
							annuallyDiscountPercentage={ tempDiscountPlaceholder }
							isPopular={ plan.isPopular }
							isFree={ plan.isFree }
							price={ prices[ plan.storeSlug ] }
							name={ plan?.title.toString() }
							isSelected={ plan.storeSlug === selectedPlanSlug }
							onSelect={ onPlanSelect }
							onPickDomainClick={ onPickDomainClick }
							onToggleExpandAll={ () => setAllPlansExpanded( ( expand ) => ! expand ) }
							disabledLabel={ disabledPlans?.[ plan.storeSlug ] }
						></PlanItem>
					)
			) }
		</div>
	);
};

export default PlansTable;
