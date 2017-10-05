package bankdroid.campaign;

import java.util.Date;
import java.util.StringTokenizer;

/**
 * @author gyenes
 * Data model to serialize for the campaigns
 */
public class CampaignStatus
{
	private Date lastShown;
	private int numberOfShow;
	private int hitCount;

	public CampaignStatus()
	{
		super();
		lastShown = new Date();
		numberOfShow = 0;
		hitCount = 0;
	}

	public Date getLastShown()
	{
		return lastShown;
	}

	public void setLastShown( final Date lastShown )
	{
		this.lastShown = lastShown;
	}

	public int getNumberOfShow()
	{
		return numberOfShow;
	}

	public void setNumberOfShow( final int numberOfShow )
	{
		this.numberOfShow = numberOfShow;
	}

	public int getHitCount()
	{
		return hitCount;
	}

	public void setHitCount( final int hitCount )
	{
		this.hitCount = hitCount;
	}

	@Override
	public String toString()
	{
		return lastShown.getTime() + "|" + numberOfShow + "|" + hitCount;
	}

	public static CampaignStatus fromString( final String string )
	{
		final StringTokenizer tokenizer = new StringTokenizer(string, "|");

		final CampaignStatus status = new CampaignStatus();

		if ( tokenizer.countTokens() == 3 )
		{
			final long time = Long.parseLong(tokenizer.nextToken());
			status.setLastShown(new Date(time));
			final int shows = Integer.parseInt(tokenizer.nextToken());
			status.setNumberOfShow(shows);
			final int hits = Integer.parseInt(tokenizer.nextToken());
			status.setHitCount(hits);
		}
		return status;
	}
}
