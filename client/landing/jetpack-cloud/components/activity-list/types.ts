export type ActivityDescriptionPart = {
	children: string[];
	intent?: string;
	section?: string;
	type: string;
};

export type LogItemType = {
	activityId: string;
	activityTitle: string;
	activityStatus: string;
	activityDate: string;
	activityDescription: ActivityDescriptionPart[];
	activityName: string;
	actorRole: string;
	actorType: 'Application' | 'Happiness Engineer' | 'Person';
	actorAvatarUrl: string;
	actorName: string;
	actorWpcomId: number;
};

export type LogData = {
	state: string;
	data: LogItemType[];
};
